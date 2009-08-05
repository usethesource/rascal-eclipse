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
package org.dancingbear.graphbrowser.layout.dot;

import java.util.ArrayList;

import org.dancingbear.graphbrowser.layout.model.DirectedGraph;
import org.dancingbear.graphbrowser.layout.model.Edge;
import org.dancingbear.graphbrowser.layout.model.EdgeList;
import org.dancingbear.graphbrowser.layout.model.Node;
import org.dancingbear.graphbrowser.layout.model.NodeList;

/**
 * Finds a tight spanning tree from the graphs edges which induce a valid rank
 * assignment. This process requires that the nodes be initially given a
 * feasible ranking.
 * 
 * @author Randy Hudson
 * @since 2.1.2
 */
class TightSpanningTreeSolver extends SpanningTreeVisitor {

    private DirectedGraph graph;

    private ArrayList<Edge> candidates = new ArrayList<Edge>();

    private NodeList members = new NodeList();

    Node addEdge(Edge edge) {
        int delta = edge.getSlack();
        edge.setTree(true);
        Node node;
        if (edge.getTarget().isFlag()) {
            delta = -delta;
            node = edge.getSource();
            setParentEdge(node, edge);
            getSpanningTreeChildren(edge.getTarget()).add(edge);
        } else {
            node = edge.getTarget();
            setParentEdge(node, edge);
            getSpanningTreeChildren(edge.getSource()).add(edge);
        }
        members.adjustRank(delta);
        addNode(node);
        return node;
    }

    void addNode(Node node) {
        setNodeReachable(node);
        EdgeList list = node.getIncoming();
        Edge e;
        for (int i = 0; i < list.size(); i++) {
            e = list.getEdge(i);
            if (!isNodeReachable(e.getSource())) {
                if (!isCandidate(e)) {
                    setCandidate(e);
                    candidates.add(e);
                }
            } else
                candidates.remove(e);
        }

        list = node.getOutgoing();
        for (int i = 0; i < list.size(); i++) {
            e = list.getEdge(i);
            if (!isNodeReachable(e.getTarget())) {
                if (!isCandidate(e)) {
                    setCandidate(e);
                    candidates.add(e);
                }
            } else
                candidates.remove(e);
        }
        members.add(node);
    }

    void init() {
        graph.resetEdgeFlags(true);
        graph.resetNodeFlags();
        for (int i = 0; i < graph.getNodes().size(); i++) {
            Node node = graph.getNodes().get(i);
            node.getWorkingData()[0] = new EdgeList();
        }
    }

    private boolean isCandidate(Edge e) {
        return e.isFlag();
    }

    private boolean isNodeReachable(Node node) {
        return node.isFlag();
    }

    private void setCandidate(Edge e) {
        e.setFlag(true);
    }

    private void setNodeReachable(Node node) {
        node.setFlag(true);
    }

    private void solve() {
        Node root = graph.getNodes().getNode(0);
        setParentEdge(root, null);
        addNode(root);
        while (members.size() < graph.getNodes().size()) {
            if (candidates.size() == 0)
                throw new RuntimeException("graph is not fully connected");//$NON-NLS-1$
            int minSlack = Integer.MAX_VALUE, slack;
            Edge minEdge = null, edge;
            for (int i = 0; i < candidates.size() && minSlack > 0; i++) {
                edge = candidates.get(i);
                slack = edge.getSlack();
                if (slack < minSlack) {
                    minSlack = slack;
                    minEdge = edge;
                }
            }
            addEdge(minEdge);
        }
        NodeList temp = graph.getNodes();
        temp.normalizeRanks();
        graph.setNodes(temp);
    }

    public void visit(DirectedGraph graph) {
        this.graph = graph;
        init();
        solve();
    }

}
