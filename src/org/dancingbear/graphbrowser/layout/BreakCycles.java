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

import java.util.ArrayList;
import java.util.List;

/**
 * This visitor eliminates cycles in the graph using a "greedy" heuristic. Nodes
 * which are sources and sinks are marked and placed in a source and sink list,
 * leaving only nodes involved in cycles. A remaining node with the highest
 * (outgoing-incoming) edges score is then chosen greedily as if it were a
 * source. The process is repeated until all nodes have been marked and placed
 * in a list. The lists are then concatenated, and any edges which go backwards
 * in this list will be inverted during the layout procedure.
 * 
 * @author Daniel Lee
 * @since 2.1.2
 */
class BreakCycles extends GraphVisitor {

    // Used in identifying cycles and in cycle removal.
    // Flag field indicates "presence". If true, the node has been removed from
    // the list.
    NodeList graphNodes = new NodeList();

    private boolean allNodesFlagged() {
        for (int i = 0; i < graphNodes.size(); i++) {
            if (graphNodes.getNode(i).isFlag() == false)
                return false;
        }
        return true;
    }

    private void breakCycles(DirectedGraph g) {
        initializeDegrees(g);
        greedyCycleRemove(g);
        invertEdges(g);
    }

    /**
     * 
     * @param g the graph to check for cycles
     * @return true if g contains cycles, false otherwise
     */
    public boolean containsCycles(DirectedGraph g) {
        List<Node> noLefts = new ArrayList<Node>();
        // Identify all initial nodes for removal
        for (int i = 0; i < graphNodes.size(); i++) {
            Node node = graphNodes.getNode(i);
            if (getIncomingCount(node) == 0)
                sortedInsert(noLefts, node);
        }

        while (noLefts.size() > 0) {
            Node node = (Node) noLefts.remove(noLefts.size() - 1);
            node.setFlag(true);
            for (int i = 0; i < node.getOutgoing().size(); i++) {
                Node right = node.getOutgoing().getEdge(i).getTarget();
                setIncomingCount(right, getIncomingCount(right) - 1);
                if (getIncomingCount(right) == 0)
                    sortedInsert(noLefts, right);
            }
        }

        if (allNodesFlagged())
            return false;
        return true;
    }

    /*
     * Returns the node in graphNodes with the largest (outgoing edge count -
     * incoming edge count) value
     */
    private Node findNodeWithMaxDegree() {
        int max = Integer.MIN_VALUE;
        Node maxNode = null;

        for (int i = 0; i < graphNodes.size(); i++) {
            Node node = graphNodes.getNode(i);
            if (getDegree(node) >= max && node.isFlag() == false) {
                max = getDegree(node);
                maxNode = node;
            }
        }
        return maxNode;
    }

    private int getDegree(Node n) {
        return n.getWorkingInts()[3];
    }

    private int getIncomingCount(Node n) {
        return n.getWorkingInts()[0];
    }

    private int getInDegree(Node n) {
        return n.getWorkingInts()[1];
    }

    private int getOrderIndex(Node n) {
        return n.getWorkingInts()[0];
    }

    private int getOutDegree(Node n) {
        return n.getWorkingInts()[2];
    }

    private void greedyCycleRemove(DirectedGraph g) {
        NodeList sL = new NodeList();
        NodeList sR = new NodeList();

        do {
            // Add all sinks and isolated nodes to sR
            boolean hasSink;
            do {
                hasSink = false;
                for (int i = 0; i < graphNodes.size(); i++) {
                    Node node = graphNodes.getNode(i);
                    if (getOutDegree(node) == 0 && node.isFlag() == false) {
                        hasSink = true;
                        node.setFlag(true);
                        updateIncoming(node);
                        sR.add(node);
                        break;
                    }
                }
            } while (hasSink);

            // Add all sources to sL
            boolean hasSource;
            do {
                hasSource = false;
                for (int i = 0; i < graphNodes.size(); i++) {
                    Node node = graphNodes.getNode(i);
                    if (getInDegree(node) == 0 && node.isFlag() == false) {
                        hasSource = true;
                        node.setFlag(true);
                        updateOutgoing(node);
                        sL.add(node);
                        break;
                    }
                }
            } while (hasSource);

            // When all sinks and sources are removed, choose a node with the
            // maximum degree (outDegree - inDegree) and add it to sL
            Node max = findNodeWithMaxDegree();
            if (max != null) {
                sL.add(max);
                max.setFlag(true);
                updateIncoming(max);
                updateOutgoing(max);
            }
        } while (!allNodesFlagged());

        // Assign order indexes
        int orderIndex = 0;
        for (int i = 0; i < sL.size(); i++) {
            setOrderIndex(sL.getNode(i), orderIndex++);
        }
        for (int i = sR.size() - 1; i >= 0; i--) {
            setOrderIndex(sR.getNode(i), orderIndex++);
        }
    }

    private void initializeDegrees(DirectedGraph g) {
        graphNodes.resetFlags();
        for (int i = 0; i < g.getNodes().size(); i++) {
            Node n = graphNodes.getNode(i);
            setInDegree(n, n.getIncoming().size());
            setOutDegree(n, n.getOutgoing().size());
            setDegree(n, n.getOutgoing().size() - n.getIncoming().size());
        }
    }

    private void invertEdges(DirectedGraph g) {
        for (int i = 0; i < g.getEdges().size(); i++) {
            Edge e = g.getEdges().getEdge(i);
            if (getOrderIndex(e.getSource()) > getOrderIndex(e.getTarget())) {
                e.invert();
                e.setFeedback(true);
            }
        }
    }

    /**
     * 
     * @param g the graph to revisit
     */
    public void revisit(DirectedGraph g) {
        for (int i = 0; i < g.getEdges().size(); i++) {
            Edge e = g.getEdges().getEdge(i);
            if (e.isFeedback())
                e.invert();
        }
    }

    private void setDegree(Node n, int deg) {
        n.getWorkingInts()[3] = deg;
    }

    private void setIncomingCount(Node n, int count) {
        n.getWorkingInts()[0] = count;
    }

    private void setInDegree(Node n, int deg) {
        n.getWorkingInts()[1] = deg;
    }

    private void setOrderIndex(Node n, int index) {
        n.getWorkingInts()[0] = index;
    }

    private void setOutDegree(Node n, int deg) {
        n.getWorkingInts()[2] = deg;
    }

    private void sortedInsert(List<Node> list, Node node) {
        int insert = 0;
        while (insert < list.size()
                && ((Node) list.get(insert)).getSortValue() > node
                        .getSortValue())
            insert++;
        list.add(insert, node);
    }

    /*
     * Called after removal of n. Updates the degree values of n's incoming
     * nodes.
     */
    private void updateIncoming(Node n) {
        for (int i = 0; i < n.getIncoming().size(); i++) {
            Node in = n.getIncoming().getEdge(i).getSource();
            if (in.isFlag() == false) {
                setOutDegree(in, getOutDegree(in) - 1);
                setDegree(in, getOutDegree(in) - getInDegree(in));
            }
        }
    }

    /*
     * Called after removal of n. Updates the degree values of n's outgoing
     * nodes.
     */
    private void updateOutgoing(Node n) {
        for (int i = 0; i < n.getOutgoing().size(); i++) {
            Node out = n.getOutgoing().getEdge(i).getTarget();
            if (out.isFlag() == false) {
                setInDegree(out, getInDegree(out) - 1);
                setDegree(out, getOutDegree(out) - getInDegree(out));
            }
        }
    }

    /**
     * the cyclebreaking visit-function
     * 
     * @param g the graph to visit
     */

    public void visit(DirectedGraph g) {
        // put all nodes in list, initialize index
        graphNodes.resetFlags();
        for (int i = 0; i < g.getNodes().size(); i++) {
            Node n = g.getNodes().getNode(i);
            setIncomingCount(n, n.getIncoming().size());
            graphNodes.add(n);
        }
        if (containsCycles(g)) {
            breakCycles(g);
        }
    }

}
