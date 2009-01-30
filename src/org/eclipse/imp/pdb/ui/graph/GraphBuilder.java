package org.eclipse.imp.pdb.ui.graph;

import java.util.HashMap;

import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.swt.SWT;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;

public class GraphBuilder {
	private HashMap<String, GraphNode> fNodeCache = new HashMap<String, GraphNode>();
	private final Graph graph;
	
	public GraphBuilder(Graph graph) {
    	this.graph = graph;
	}

	public void computeGraph(IValue fact) {
    	if (canShow(fact.getType())) {
    		convertBinaryRelToGraph((IRelation) fact);
        }
    }
	
	public static boolean canShow(Type type) {
		return type.isRelationType() && type.getArity() == 2;
	}

    private void convertBinaryRelToGraph(IRelation rel) {
        fNodeCache.clear();

        for (IValue value : rel) {
        	ITuple tuple = (ITuple) value;
            GraphNode from = getOrCreateNode(tuple.get(0));
            GraphNode to = getOrCreateNode(tuple.get(1));

            if (from != null && to != null) {
                new GraphConnection(graph, SWT.NONE, from, to);
            }
        }
    }

    private GraphNode getOrCreateNode(IValue value) {
    	String nodeName = getNodeName(value);
        GraphNode node;

        if (nodeName.length() == 0) {
            return null;
        }

        if (fNodeCache.containsKey(nodeName)) {
            node = fNodeCache.get(nodeName);
        } else {
            node = new GraphNode(graph, SWT.NONE, nodeName);
            fNodeCache.put(nodeName, node);
        }

        return node;
    }
    
	private String getNodeName(IValue value) {
		return value.toString();
	}
}
