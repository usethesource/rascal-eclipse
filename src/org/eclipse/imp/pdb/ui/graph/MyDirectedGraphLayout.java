package org.eclipse.imp.pdb.ui.graph;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.swt.SWT;
import org.eclipse.zest.layouts.algorithms.DirectedGraphLayoutAlgorithm;
import org.eclipse.zest.layouts.dataStructures.InternalNode;
import org.eclipse.zest.layouts.dataStructures.InternalRelationship;

public class MyDirectedGraphLayout extends DirectedGraphLayoutAlgorithm {

	public MyDirectedGraphLayout(int styles) {
		super(styles);
	}

	protected void applyLayoutInternal(InternalNode[] entitiesToLayout, InternalRelationship[] relationshipsToConsider, double boundsX, double boundsY, double boundsWidth, double boundsHeight) {
		Map<InternalNode, Node> mapping = new HashMap<InternalNode, Node>(entitiesToLayout.length);
		DirectedGraph graph = new DirectedGraph();
		
		for (InternalNode internalNode : entitiesToLayout) {
			Node node = new Node(internalNode);
			node.setSize(new Dimension(10, 10));
			mapping.put(internalNode, node);
			addNode(graph, node);
		}
		
		for (InternalRelationship relationship : relationshipsToConsider) {
			Node source = (Node) mapping.get(relationship.getSource());
			Node dest = (Node) mapping.get(relationship.getDestination());
			Edge edge = new Edge(relationship, source, dest);
			addEdge(graph, edge);
		}
		
		DirectedGraphLayout directedGraphLayout = new DirectedGraphLayout();
		directedGraphLayout.visit(graph);

		for (Object o : graph.nodes) {
			Node node = (Node) o;
			InternalNode internalNode = (InternalNode) node.data;
			// For horizontal layout transpose the x and y coordinates
			if ((layout_styles & SWT.HORIZONTAL) == SWT.HORIZONTAL) {
				internalNode.setInternalLocation(node.y, node.x);
			}else {
				internalNode.setInternalLocation(node.x, node.y);
			}
		}
		updateLayoutLocations(entitiesToLayout);
	}

	@SuppressWarnings("unchecked")
	private boolean addEdge(DirectedGraph graph, Edge edge) {
		return graph.edges.add(edge);
	}

	@SuppressWarnings("unchecked")
	private boolean addNode(DirectedGraph graph, Node node) {
		return graph.nodes.add(node);
	}

}
