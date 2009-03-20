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

/**
 * Some utility methods for graphs.
 * 
 * @author Eric Bordeau
 * @since 2.1.2
 */
class GraphUtilities {

    static Subgraph getCommonAncestor(Node left, Node right) {
        Subgraph parent;
        if (right instanceof Subgraph)
            parent = (Subgraph) right;
        else
            parent = right.getParent();
        while (parent != null) {
            if (parent.isNested(left))
                return parent;
            parent = parent.getParent();
        }
        return null;
    }

    static boolean isConstrained(Node left, Node right) {
        Subgraph common = left.getParent();
        while (common != null && !common.isNested(right)) {
            left = left.getParent();
            common = left.getParent();
        }
        while (right.getParent() != common)
            right = right.getParent();
        return (left.getRowOrder() != -1 && right.getRowOrder() != -1)
                && left.getRowOrder() != right.getRowOrder();
    }

    /**
     * Returns <code>true</code> if the given graph contains at least one cycle.
     * 
     * @param graph the graph to test
     * @return whether the graph is cyclic
     */
    public static boolean isCyclic(DirectedGraph graph) {
        return isCyclic(new NodeList(graph.getNodes()));
    }

    /**
     * Recursively removes leaf nodes from the list until there are no nodes
     * remaining (acyclic) or there are no leaf nodes but the list is not empty
     * (cyclic), then returns the result.
     * 
     * @param nodes the list of nodes to test
     * @return whether the graph is cyclic
     */
    public static boolean isCyclic(NodeList nodes) {
        if (nodes.isEmpty())
            return false;
        int size = nodes.size();
        // remove all the leaf nodes from the graph
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.getNode(i);
            if (node.getOutgoing() == null || node.getOutgoing().isEmpty()) { // this
                                                                              // is
                                                                              // a
                // leaf node
                nodes.remove(node);
                for (int j = 0; j < node.getIncoming().size(); j++) {
                    Edge edge = node.getIncoming().getEdge(j);

                    Node sourceNode = edge.getSource();
                    sourceNode.removeOutgoingEdge(edge);
                    edge.setSource(sourceNode);
                }
            }
        }
        // if no nodes were removed, that means there are no leaf nodes and the
        // graph is cyclic
        if (nodes.size() == size)
            return true;
        // leaf nodes were removed, so recursively call this method with the new
        // list
        return isCyclic(nodes);
    }

    /**
     * Counts the number of edge crossings in a DirectedGraph
     * 
     * @param graph the graph whose crossed edges are counted
     * @return the number of edge crossings in the graph
     */
    public static int numberOfCrossingsInGraph(DirectedGraph graph) {
        int crossings = 0;
        for (int i = 0; i < graph.getRanks().size(); i++) {
            Rank rank = graph.getRanks().getRank(i);
            crossings += numberOfCrossingsInRank(rank);
        }
        return crossings;
    }

    /**
     * Counts the number of edge crossings in a Rank
     * 
     * @param rank the rank whose crossed edges are counted
     * @return the number of edge crossings in the rank
     */
    public static int numberOfCrossingsInRank(Rank rank) {
        int crossings = 0;
        for (int i = 0; i < rank.size() - 1; i++) {
            Node currentNode = rank.getNode(i);
            Node nextNode;
            for (int j = i + 1; j < rank.size(); j++) {
                nextNode = rank.getNode(j);
                EdgeList currentOutgoing = currentNode.getOutgoing();
                EdgeList nextOutgoing = nextNode.getOutgoing();
                for (int k = 0; k < currentOutgoing.size(); k++) {
                    Edge currentEdge = currentOutgoing.getEdge(k);
                    for (int l = 0; l < nextOutgoing.size(); l++) {
                        if (nextOutgoing.getEdge(l).getIndexForRank(
                                currentNode.getRank() + 1) < currentEdge
                                .getIndexForRank(currentNode.getRank() + 1))
                            crossings++;
                    }
                }
            }
        }
        return crossings;
    }

    private static NodeList search(Node node, NodeList list) {
        if (node.isFlag())
            return list;
        node.setFlag(true);
        list.add(node);
        for (int i = 0; i < node.getOutgoing().size(); i++)
            search(node.getOutgoing().getEdge(i).getTarget(), list);
        return list;
    }

    /**
     * Returns <code>true</code> if adding an edge between the 2 given nodes
     * will introduce a cycle in the containing graph.
     * 
     * @param source the potential source node
     * @param target the potential target node
     * @return whether an edge between the 2 given nodes will introduce a cycle
     */
    public static boolean willCauseCycle(Node source, Node target) {
        NodeList nodes = search(target, new NodeList());
        nodes.resetFlags();
        return nodes.contains(source);
    }

}
