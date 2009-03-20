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
package org.dancingbear.graphbrowser.layout;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;

/**
 * A Node which may contain other nodes. A Subgraph is a compound or container
 * node. It may have incoming and outgoing edges just like a node. Subgraphs are
 * used in {@link CompoundDirectedGraph}s. A proper layout of a compound graph
 * ensures that all of a subgraph's children are placed inside its rectangular
 * region. Nodes which do not belong to the subgraph must be placed outside that
 * region.
 * <P>
 * A Subgraph may contain another Subgraph.
 * <P>
 * A Subgraph has additional geometric properties which describe the containing
 * box. They are:
 * <UL>
 * <LI>{@link #insets} - the size of the subgraph's border. A subgraph is
 * typically rendered as a thin rectangular box. Sometimes this box is labeled
 * or decorated. The insets can be used to reserve space for this purpose.
 * <LI>{@link #innerPadding} - the amount of empty space that must be preserved
 * just inside the subgraph's border. This is the minimum space between the
 * border, and the children node's contained inside the subgraph.
 * </UL>
 * 
 * @author hudsonr
 * @since 2.1.2
 */
public class Subgraph extends Node {

    // the allowed values which describe *HOW* to compute the rank itself.
    // These are only used when the node is in a subgraph.
    // NONE means that the ranktype is not determined yet (in a node which
    // resides in a subgraph), or not used at all (an ordinary Node)
    public enum RankType {
        MAX, MIN, SOURCE, SINK, SAME, NONE
    }

    protected RankType rankType;

    /**
     * The children of this subgraph. Nodes may not belong to more than one
     * subgraph.
     */
    private NodeList members = new NodeList();
    private Node left = null, right = null;

    Node head;
    Node tail;

    int nestingTreeMin;

    /**
     * The space required for this subgraph's border. The default value is
     * undefined.
     */
    public Insets insets = new Insets(1);

    /**
     * The minimum space between this subgraph's border and it's children.
     */
    public Insets innerPadding = NO_INSETS;
    private boolean cluster;

    private static final Insets NO_INSETS = new Insets();

    /**
     * Constructs a new subgraph with the given data object.
     * 
     * @see Node#Node(Object)
     * @param data an arbitrary data object
     */
    public Subgraph(Object data) {
        this(data, null);
    }

    /**
     * Constructs a new subgraph with the given data object and parent subgraph.
     * 
     * @see Node#Node(Object, Subgraph)
     * @param data an arbitrary data object
     * @param parent the parent
     */
    public Subgraph(Object data, Subgraph parent) {
        super(data, parent);
    }

    public void setRank(int rank) {
        super.setRank(rank);

        if (null == getProperties()) {
            return;
        }

        for (Node node : members) {
            if ("same".equals(getProperties().get("rank"))) {
                node.setRank(rank);
            }
        }
    }

    @Override
    public void setX(int xValue) {
        super.setX(xValue);
        for (Node node : members) {
            node.addToX(xValue);
        }
    }

    @Override
    public void setY(int yValue) {
        super.setY(yValue);
        for (Node node : members) {
            node.addToY(yValue);
        }
    }

    /**
     * Adds the given node to this subgraph.
     * 
     * @param n the node to add
     */
    public void addMember(Node n) {
        members.add(n);
        n.setParent(this);
    }

    /**
     * Returns <code>true</code> if the given node is contained inside the
     * branch represented by this subgraph.
     * 
     * @param n the node in question
     * @return <code>true</code> if nested
     */
    boolean isNested(Node n) {
        return n.nestingIndex >= nestingTreeMin
                && n.nestingIndex <= nestingIndex;
    }

    public Node getHead() {
        return head;
    }

    public Node getTail() {
        return tail;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public NodeList getMembers() {
        return members;
    }

    public void setMembers(NodeList members) {
        this.members = members;

    }

    public void setCluster(boolean cluster) {
        this.cluster = cluster;
    }

    public boolean isCluster() {
        return cluster;
    }

    // This function is being used to fix the internal ranking of a subgraph.
    // All nodes will be scanned by the function ,and according to the declared
    // RankType, the ranks will be set.
    public void fixInternalRanks(int rank) {

        // int subgraphrank = -1;

        // subgraphrank = this.getRank();

        // let's make sure the ranking is NOT invalid
        /*
         * if (subgraphrank == -1) {
         * System.err.println("Invalid rank for this subgraph: " +
         * this.toString()); System.exit(1); }
         */
        if (getRankType() == RankType.SAME) {
            fixInternalRanksSAME(rank);
        }
    }

    // runs the SAME rank assignment on the subgraph.
    private void fixInternalRanksSAME(int subgraphrank) {
        setContainedAllNodesToRank(subgraphrank);
    }

    // TODO
    // runs the MIN rank assignment on the subgraph.
    private void fixInternalRanksMIN(int subgraphrank) {
        setContainedAllNodesToRank(subgraphrank);
    }

    // TODO
    // runs the MAX rank assignment on the subgraph.
    private void fixInternalRanksMAX(int subgraphrank) {
        setContainedAllNodesToRank(subgraphrank);
    }

    // runs the SOURCE rank assignment on the subgraph.
    private void fixInternalRanksSOURCE(int subgraphrank) {
        setContainedAllNodesToRank(subgraphrank);
    }

    // runs the SINK rank assignment on the subgraph.
    private void fixInternalRanksSINK(int subgraphrank) {
        setContainedAllNodesToRank(subgraphrank);
    }

    // this function sets all nodes in this subgraph to the specified rank
    private void setContainedAllNodesToRank(int rank) {
        for (Node member : getMembers()) {
            member.setRank(rank);
        }
    }

    public RankType getRankType() {

        if (null == rankType) {
            this.setRankType(RankType.NONE);

            if (getProperties().containsKey("rank")) {
                if ("same".equals(getProperties().get("rank"))) {
                    setRankType(RankType.SAME);
                } else if ("sink".equals(getProperties().get("sink"))) {
                    setRankType(RankType.SINK);
                } else if ("source".equals(getProperties().get("source"))) {
                    setRankType(RankType.SOURCE);
                } else if ("max".equals(getProperties().get("max"))) {
                    setRankType(RankType.MAX);
                } else if ("min".equals(getProperties().get("min"))) {
                    setRankType(RankType.MIN);
                }

            }
        }

        return rankType;
    }

    public void setRankType(RankType rankType) {
        this.rankType = rankType;
    }
}
