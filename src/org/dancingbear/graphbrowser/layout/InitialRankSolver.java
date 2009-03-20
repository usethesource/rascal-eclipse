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
import java.util.Stack;

/**
 * Assigns a valid rank assignment to all nodes based on their edges. The
 * assignment is not optimal in that it does not provide the minimum global
 * length of edge lengths.
 * 
 * @author Randy Hudson
 * @since 2.1.2
 */
class InitialRankSolver extends GraphVisitor {

    private DirectedGraph graph;

    // private EdgeList candidates = new EdgeList();
    // private NodeList members = new NodeList();

    private void assignMinimumRank(Node node) {
        int rank = 0;
        for (Edge edge : node.getIncoming()) {
            rank = Math.max(rank, edge.getDelta() + edge.getSource().getRank());
        }
        node.setRank(rank);
    }

    private void connectForest() {
        List<NodeList> forest = new ArrayList<NodeList>();
        Stack<Node> stack = new Stack<Node>();
        NodeList tree;
        graph.resetNodeFlags();
        for (int i = 0; i < graph.getNodes().size(); i++) {
            Node neighbor, n = graph.getNodes().getNode(i);
            if (n.isFlag())
                continue;
            tree = new NodeList();
            stack.push(n);
            while (!stack.isEmpty()) {
                n = (Node) stack.pop();
                n.setFlag(true);
                tree.add(n);
                for (int s = 0; s < n.getIncoming().size(); s++) {
                    neighbor = n.getIncoming().getEdge(s).getSource();
                    if (!neighbor.isFlag())
                        stack.push(neighbor);
                }
                for (int s = 0; s < n.getOutgoing().size(); s++) {
                    neighbor = n.getOutgoing().getEdge(s).getTarget();
                    if (!neighbor.isFlag())
                        stack.push(neighbor);
                }
            }
            forest.add(tree);
        }

        if (forest.size() > 1) {
            // connect the forest
            graph.setForestRoot(new Node("the forest root")); //$NON-NLS-1$
            graph.addNode(graph.getForestRoot());
            for (int i = 0; i < forest.size(); i++) {
                tree = (NodeList) forest.get(i);
                graph.addEdge(new Edge(graph.getForestRoot(), tree.getNode(0),
                        0, 0));
            }
        }
    }

    private void solve() {
        NodeList graphNodes = graph.getNodes();
        if (!graphNodes.isEmpty()) {
            // Not yet ranked nodes
            NodeList unranked = new NodeList(graphNodes);

            while (!unranked.isEmpty()) {
                NodeList rankMe = new NodeList(); // Nodes to be ranked
                for (Node node : unranked) {
                    EdgeList incoming = getIncomingEdges(node, graphNodes);
                    if (incoming.isEmpty() || incoming.isCompletelyFlagged()) {
                        rankMe.add(node);
                    }
                }

                if (rankMe.isEmpty()) {
                    throw new RuntimeException("Cycle detected in graph"); //$NON-NLS-1$
                }
                for (Node node : rankMe) {
                    assignMinimumRank(node);
                    node.getOutgoing().setFlags(true);
                }

                unranked.removeAll(rankMe);
            }
        }
    }

    private EdgeList getIncomingEdges(Node node, NodeList graphNodes) {
        EdgeList incoming = new EdgeList();
        for (Edge in : node.getIncoming()) {
            if (graphNodes.contains(in.getSource())) {
                incoming.add(in);
            }
        }

        return incoming;
    }

    private void solveSubgraphs() {
        if (!graph.getNodes().isEmpty()) {
            NodeList unranked = new NodeList(graph.getNodes());

            for (Node node : unranked) {

                if (node instanceof Subgraph) {

                    if ("same".equals(node.getProperties().get("rank"))) {
                        for (Node member : ((Subgraph) node).getMembers()) {
                            member.setRank(node.getRank());

                        }
                    }

                    // TODO not the best place, nodes are already ranked!

                }
                if (node.getData() instanceof Subgraph) {
                    if ("same".equals(((Node) node.getData()).getProperties()
                            .get("rank"))) {
                        for (Node member : ((Subgraph) node.getData())
                                .getMembers()) {
                            member.setRank(node.getRank());

                        }
                    }

                }
            }
        }

    }

    /**
     * The visit function for solving the initial ranks on a directed graph
     * 
     * @param graph the DirectedGraph to visit
     */
    public void visit(DirectedGraph graph) {
        this.graph = graph;
        graph.resetEdgeFlags(false);
        graph.resetNodeFlags();
        solve();
        // solveSubgraphs();
        connectForest();
    }

}
