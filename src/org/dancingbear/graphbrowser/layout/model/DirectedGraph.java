/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.dancingbear.graphbrowser.layout.model;

import java.util.List;
import java.util.Map;

import org.dancingbear.graphbrowser.layout.dot.Rank;
import org.dancingbear.graphbrowser.layout.dot.RankList;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutGraph;
import org.eclipse.zest.layouts.LayoutRelationship;

/**
 * A graph consisting of nodes and directed edges. A DirectedGraph serves as the
 * input to a graph layout algorithm. The algorithm will place the graph's nodes
 * and edges according to certain goals, such as short, non-crossing edges, and
 * readability.
 * 
 * @author hudsonr
 * @since 2.1.2
 */
public class DirectedGraph implements LayoutGraph {

	private int direction = PositionConstants.SOUTH;

	/**
	 * The default padding to be used for nodes which don't specify any padding.
	 * Padding is the amount of empty space to be left around a node. The
	 * default value is undefined.
	 */
	private Insets defaultPadding = new Insets(16);

	/**
	 * All of the edges in the graph.
	 */
	private EdgeList edges = new EdgeList();

	/**
	 * All of the nodes in the graph.
	 */
	private NodeList nodes = new NodeList();

	/**
	 * For internal use only. The list of rows which makeup the final graph
	 * layout.
	 * 
	 * @deprecated
	 */
	private RankList ranks = new RankList();

	private Node forestRoot;
	private Insets margin = new Insets();
	private int[] rankLocations;
	private int[][] cellLocations = null;
	private int tensorStrength;
	private int tensorSize;
	private Dimension size = new Dimension();

	private Map<String, String> properties;

	/**
	 * When DirectedGraph used as Subgraph, this is the original Subgraph
	 */
	private Subgraph subgraph;

	public DirectedGraph() {
		super();
	}

	int[] getCellLocations(int rank) {
		return cellLocations[rank];
	}

	/**
	 * Returns the default padding for nodes.
	 * 
	 * @return the default padding
	 * @since 3.2
	 */
	public Insets getDefaultPadding() {
		return defaultPadding;
	}

	/**
	 * Returns the direction in which the graph will be layed out.
	 * 
	 * @return the layout direction
	 * @since 3.2
	 */
	public int getDirection() {
		return direction;
	}

	public Dimension getLayoutSize() {
		return getSize();
	}

	/**
	 * Sets the outer margin for the entire graph. The margin is the space in
	 * which nodes should not be placed.
	 * 
	 * @return the graph's margin
	 * @since 3.2
	 */
	public Insets getMargin() {
		return margin;
	}

	public Node getNode(int rank, int index) {
		if (ranks.size() <= rank)
			return null;
		Rank r = ranks.getRank(rank);
		if (r.size() <= index)
			return null;
		return r.getNode(index);
	}

	/**
	 * Returns the effective padding for the given node. If the node has a
	 * specified padding, it will be used, otherwise, the graph's defaultPadding
	 * is returned. The returned value must not be modified.
	 * 
	 * @param node the node
	 * @return the effective padding for that node
	 */
	public Insets getPadding(Node node) {
		Insets pad = node.getPadding();
		if (pad == null)
			return defaultPadding;
		return pad;
	}

	public int[] getRankLocations() {
		return rankLocations;
	}

	/**
	 * Removes the given edge from the graph.
	 * 
	 * @param edge the edge to be removed
	 */
	public void removeEdge(Edge edge) {
		edges.remove(edge);

		Node sourceNode = edge.getSource();
		sourceNode.removeOutgoingEdge(edge);
		edge.setSource(sourceNode);
		// edge.source.outgoing.remove(edge);

		edge.getTarget().getIncoming().remove(edge);
		if (edge.getVNodes() != null)
			for (int j = 0; j < edge.getVNodes().size(); j++)
				removeNode(edge.getVNodes().getNode(j));
	}

	/**
	 * Removes the given node from the graph. Does not remove the node's edges.
	 * 
	 * @param node the node to remove
	 */
	public void removeNode(Node node) {
		nodes.remove(node);
		if (ranks != null)
			ranks.getRank(node.getRank()).remove(node);
	}

	/**
	 * Sets the default padding for all nodes in the graph. Padding is the empty
	 * space left around the <em>outside</em> of each node. The default padding
	 * is used for all nodes which do not specify a specific amount of padding
	 * (i.e., their padding is <code>null</code>).
	 * 
	 * @param insets the padding
	 */
	public void setDefaultPadding(Insets insets) {
		defaultPadding = insets;
	}

	// public void setGraphTensor(int length, int strength) {
	// tensorStrength = strength;
	// tensorSize = length;
	// }

	/**
	 * Sets the layout direction for the graph. Edges will be layed out in the
	 * specified direction (unless the graph contains cycles). Supported values
	 * are:
	 * <UL>
	 * <LI>{@link org.eclipse.draw2d.PositionConstants#EAST}
	 * <LI>{@link org.eclipse.draw2d.PositionConstants#SOUTH}
	 * </UL>
	 * <P>
	 * The default direction is south.
	 * 
	 * @param direction the layout direction
	 * @since 3.2
	 */
	public void setDirection(int direction) {
		this.direction = direction;
	}

	/**
	 * Sets the graphs margin.
	 * 
	 * @param insets the graph's margin
	 * @since 3.2
	 */
	public void setMargin(Insets insets) {
		this.margin = insets;
	}

	public EdgeList getEdges() {
		return edges;
	}

	public void setEdges(EdgeList edges) {
		this.edges = edges;
	}

	public NodeList getNodes() {
		return nodes;
	}

	public void setNodes(NodeList nodes) {
		this.nodes = nodes;
	}

	public RankList getRanks() {
		return ranks;
	}

	public void setRanks(RankList ranks) {
		this.ranks = ranks;
	}

	/**
	 * adjust the rank of the graph's nodes
	 * 
	 * @param delta the amount by which to adjust
	 */
	public void adjustRank(int delta) {
		this.nodes.adjustRank(delta);
	}

	public void resetNodeFlags() {
		nodes.resetFlags();
	}

	public void addNode(Node node) {
		nodes.add(node);
	}

	public void resetEdgeFlags(boolean resetTree) {
		edges.resetFlags(resetTree);
	}

	public void addEdge(Edge e) {
		edges.add(e);
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setForestRoot(Node forestRoot) {
		this.forestRoot = forestRoot;
	}

	public Node getForestRoot() {
		return forestRoot;
	}

	public void setRankLocations(int[] rankLocations) {
		this.rankLocations = rankLocations;
	}

	public void setCellLocations(int[][] cellLocations) {
		this.cellLocations = cellLocations;
	}

	public int[][] getCellLocations() {
		return cellLocations;
	}

	void setTensorStrength(int tensorStrength) {
		this.tensorStrength = tensorStrength;
	}

	public int getTensorStrength() {
		return tensorStrength;
	}

	void setTensorSize(int tensorSize) {
		this.tensorSize = tensorSize;
	}

	public int getTensorSize() {
		return tensorSize;
	}

	void setSize(Dimension size) {
		this.size = size;
	}

	public Dimension getSize() {
		return size;
	}

	/**
	 * Check whether this graph is a subgraph
	 * 
	 * @return if this graph has a subgraph , return true
	 */
	 public boolean isSubgraph() {
		 return null != subgraph;
	 }

	public Subgraph getSubgraph() {
		return subgraph;
	}

	public void setSubgraph(Subgraph subgraph) {
		this.subgraph = subgraph;
	}

	public void addEntity(LayoutEntity arg0) {
		if (arg0 instanceof Node) {
			addNode((Node) arg0);
		}
	}

	public void addRelationship(LayoutRelationship arg0) {
		if (arg0 instanceof Edge) {
			addEdge((Edge) arg0);
		}
	}

	public List getEntities() {
		return getNodes();
	}

	public List getRelationships() {
		return getEdges();
	}

	public boolean isBidirectional() {
			return false;
	}
	
}
