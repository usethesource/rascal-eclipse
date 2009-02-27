package org.eclipse.imp.pdb.ui.graph;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.graph.DirectedGraph;
import org.eclipse.draw2d.graph.DirectedGraphLayout;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.zest.layouts.algorithms.AbstractLayoutAlgorithm;
import org.eclipse.zest.layouts.dataStructures.InternalNode;
import org.eclipse.zest.layouts.dataStructures.InternalRelationship;

/**
 * This class is partially copied from Zest DirectedGraphLayoutAlgorithm.
 * It removes the re-ordering of the algorithm's steps, but its still a bridge
 * between Zest and draw2d.graph
 */
public class DirectedGraphLayoutAlgorithm extends AbstractLayoutAlgorithm {

	public DirectedGraphLayoutAlgorithm(int styles) {
		super(styles);
	}

	@SuppressWarnings("unchecked")
	protected void applyLayoutInternal(InternalNode[] entitiesToLayout, InternalRelationship[] relationshipsToConsider, double boundsX, double boundsY, double boundsWidth, double boundsHeight) {
		HashMap<InternalNode, Node> mapping = new HashMap<InternalNode, Node>(entitiesToLayout.length);
		DirectedGraph graph = new DirectedGraph();
		for (int i = 0; i < entitiesToLayout.length; i++) {
			InternalNode internalNode = entitiesToLayout[i];
			Node node = new Node(internalNode);
			node.setSize(new Dimension(10, 10));
			mapping.put(internalNode, node);
			graph.nodes.add(node);
		}
		for (int i = 0; i < relationshipsToConsider.length; i++) {
			InternalRelationship relationship = relationshipsToConsider[i];
			Node source = mapping.get(relationship.getSource());
			Node dest = mapping.get(relationship.getDestination());
			Edge edge = new Edge(relationship, source, dest);
			graph.edges.add(edge);
		}
		DirectedGraphLayout directedGraphLayout = new DirectedGraphLayout();
		directedGraphLayout.visit(graph);

		for (Iterator iterator = graph.nodes.iterator(); iterator.hasNext();) {
			Node node = (Node) iterator.next();
			InternalNode internalNode = (InternalNode) node.data;
			internalNode.setInternalLocation(node.x, node.y);
		}
		updateLayoutLocations(entitiesToLayout);
	}

	protected int getCurrentLayoutStep() {
		return 0;
	}

	protected int getTotalNumberOfLayoutSteps() {
		return 0;
	}

	protected boolean isValidConfiguration(boolean asynchronous, boolean continuous) {
		return true;
	}

	protected void postLayoutAlgorithm(InternalNode[] entitiesToLayout, InternalRelationship[] relationshipsToConsider) {
	}

	protected void preLayoutAlgorithm(InternalNode[] entitiesToLayout, InternalRelationship[] relationshipsToConsider, double x, double y, double width, double height) {
	}

	public void setLayoutArea(double x, double y, double width, double height) {
	}

}
