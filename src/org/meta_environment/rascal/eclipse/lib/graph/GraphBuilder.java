package org.meta_environment.rascal.eclipse.lib.graph;

import java.util.HashMap;
import java.util.Iterator;

import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.INode;
import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;

public class GraphBuilder {
	private HashMap<IValue, IModelNode> fNodeCache = new HashMap<IValue, IModelNode>();
	private final IModelGraph graph;
	
	public GraphBuilder(IModelGraph graph) {
    	this.graph = graph;
	}

	public void computeGraph(IValue fact) {
		fNodeCache = new HashMap<IValue, IModelNode>();
    	convert(fact);
    }
	
	private IModelNode convert(IValue fact) {
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

	private IModelNode convertMap(IMap fact) {
		IModelNode root = getOrCreateNode(fact, "(...)");
		
		for (IValue key : fact) {
	        IModelNode from = convert(key);
	        IModelNode to = convert(fact.get(key));

	        if (from != null && to != null) {
	        	graph.addEdge(root, from);
	        	graph.addEdge(from, to);
	        }
	    }
		
		return root;
	}

	private IModelNode convertTuple(ITuple fact) {
		IModelNode node = getOrCreateNode(fact, "<...>");
		
		for (int i = 0; i < fact.arity(); i++) {
			graph.addEdge(node, convert(fact.get(i)));
		}
		
		return node;
	}

	private IModelNode convertTree(INode fact) {
		IModelNode node = getOrCreateNode(fact, fact.getName());
		
		for (IValue child : fact) {
			graph.addEdge(node, convert(child));
		}
		
		return node;
	}

	private IModelNode convertRelation(IValue fact) {
		Type type = fact.getType();
		IRelation rel = (IRelation) fact;
		
		if (type.getArity() == 2) {
			for (IValue value : rel) {
		    	ITuple tuple = (ITuple) value;
		    	IModelNode from = convert(tuple.get(0));
		    	IModelNode to = convert(tuple.get(1));

		        if (from != null && to != null) {
		        	graph.addEdge(from, to);
		        }
		    }
			
			IModelNode root = getOrCreateNode(fact, "{...}");
			ISet top = rel.domain().subtract(rel.range());
			
			for (IValue elem : top) {
				IModelNode to = convert(elem);
				graph.addEdge(root, to);
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

	private IModelNode convertSet(IValue fact) {
		return convertIterator(((ISet) fact).iterator(), fact, "{...}");
	}

	private IModelNode convertIterator(Iterator<IValue> iterator, IValue value, String label) {
		IModelNode root = getOrCreateNode(value, label);
		
		while (iterator.hasNext()) {
			IValue e = iterator.next();
			graph.addEdge(root, convert(e));
		}
		
		return root;
	}

	public static boolean canShow(Type type) {
		return true;
	}

    private IModelNode getOrCreateNode(IValue value, String label) {
    	IModelNode node;
    	
        if (fNodeCache.containsKey(value)) {
            node = fNodeCache.get(value);
        } 
        else {
            node = graph.addNode(label);
            fNodeCache.put(value, node);
        }

        return node;
    }
}
