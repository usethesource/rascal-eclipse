package org.eclipse.imp.pdb.ui.graph;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.INode;
import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.swt.SWT;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;

public class GraphBuilder {
	private HashMap<IValue, GraphNode> fNodeCache = new HashMap<IValue, GraphNode>();
	private final Graph graph;
	
	public GraphBuilder(Graph graph) {
    	this.graph = graph;
	}

	public void computeGraph(IValue fact) {
		fNodeCache.clear();
    	convert(fact);
    }
	
	private GraphNode convert(IValue fact) {
		Type type = fact.getType();
		
		if (type.isRelationType()) {
			return convertRelation(fact);
		}
		else if (type.isSetType()) {
			return convertIterator(((ISet) fact).iterator(), fact, "{...}");
		}
		else if (type.isListType()) {
			return convertIterator(((IList) fact).iterator(), fact, "[...]");
		}
		else if (type.isNodeType() || type.isAbstractDataType() || type.isConstructorType()) {
			return convertTree((INode) fact);
		}
		else if (type.isTupleType()) {
			return convertTuple((ITuple) fact);
		}
		else if (type.isMapType()) {
			return convertMap((IMap) fact);
		}
		else {
			return getOrCreateNode(fact, fact.toString());
		}
	}

	private GraphNode convertMap(IMap fact) {
		GraphNode root = getOrCreateNode(fact, "(...)");
		
		for (IValue key : fact) {
	        GraphNode from = convert(key);
	        GraphNode to = convert(fact.get(key));

	        if (from != null && to != null) {
	        	new GraphConnection(graph, SWT.NONE, root, from);
	            new GraphConnection(graph, SWT.NONE, from, to);
	        }
	    }
		
		return root;
	}

	private GraphNode convertTuple(ITuple fact) {
		GraphNode node = getOrCreateNode(fact, "<...>");
		
		for (int i = 0; i < fact.arity(); i++) {
			new GraphConnection(graph, SWT.NONE, node, convert(fact.get(i)));
		}
		
		return node;
	}

	private GraphNode convertTree(INode fact) {
		GraphNode node = new GraphNode(graph, SWT.NONE, fact.getName());
		
		for (IValue child : fact) {
			new GraphConnection(graph, SWT.NONE, node, convert(child));
		}
		
		return node;
	}

	private GraphNode convertRelation(IValue fact) {
		Type type = fact.getType();
		IRelation rel = (IRelation) fact;
		
		if (type.getArity() == 2) {
			ITuple tuple = null;
			
			for (IValue value : rel) {
		    	tuple = (ITuple) value;
		        GraphNode from = convert(tuple.get(0));
		        GraphNode to = convert(tuple.get(1));

		        if (from != null && to != null) {
		            new GraphConnection(graph, SWT.NONE, from, to);
		        }
		    }
			
			GraphNode root = getOrCreateNode(fact, fact.getType().toString());
			ISet top = rel.range().subtract(rel.domain());
			
			if (top.isEmpty() && tuple != null) {
				new GraphConnection(graph, SWT.NONE, root, convert(tuple.get(0)));
			}
			else {
				for (IValue elem : top) {
					GraphNode to = convert(elem);
					new GraphConnection(graph, SWT.NONE, root, to);
				}
			}
			
			return root;
		}
		else if (rel.arity() == 3) { // assume labeled graph
			return convertRelation(rel.select(0,2));
		}
		else {
			return convertSet(fact);
		}
			
	}

	private GraphNode convertSet(IValue fact) {
		return convertIterator(((ISet) fact).iterator(), fact, fact.getType().toString());
	}

	private GraphNode convertIterator(Iterator<IValue> iterator, IValue value, String label) {
		GraphNode root = getOrCreateNode(value, label);
		
		while (iterator.hasNext()) {
			IValue e = iterator.next();
			new GraphConnection(graph, SWT.NONE, root, convert(e));
		}
		
		return root;
	}

	public static boolean canShow(Type type) {
		return true;
	}

    private GraphNode getOrCreateNode(IValue value, String label) {
    	GraphNode node;
    	
        if (fNodeCache.containsKey(value)) {
            node = fNodeCache.get(value);
        } 
        else {
            node = new GraphNode(graph, SWT.NONE, label);
            fNodeCache.put(value, node);
        }

        return node;
    }
}
