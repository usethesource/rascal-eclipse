/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation, University of Amsterdam and others.
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
import java.util.List;

import org.dancingbear.graphbrowser.layout.Layout;
import org.dancingbear.graphbrowser.layout.model.DirectedGraph;
import org.dancingbear.graphbrowser.layout.model.Edge;
import org.dancingbear.graphbrowser.layout.model.EdgeList;
import org.dancingbear.graphbrowser.layout.model.Node;
import org.dancingbear.graphbrowser.layout.model.NodeList;
import org.dancingbear.graphbrowser.layout.model.Subgraph;

/**
 * Performs a graph layout of a <code>DirectedGraph</code>. The directed graph
 * must meet the following conditions:
 * <UL>
 * <LI>The graph must be connected.
 * <LI>All edge's must be added to the graph's {@link DirectedGraph#edges edges}
 * list exactly once.
 * <LI>All nodes must be added to the graph's {@link DirectedGraph#nodes nodes}
 * list exactly once.
 * </UL>
 * 
 * This algorithm will:
 * <UL>
 * <LI>break cycles by inverting a set of feedback edges. Feedback edges will
 * have the flag {@link Edge#isFeedback} set to <code>true</code>. The following
 * statements are true with respect to the inverted edge. When the algorithm
 * completes, it will invert the edges again, but will leave the feedback flags
 * set.
 * <LI>for each node <em>n</em>, assign n to a "rank" R(n), such that: for each
 * edge (m, n) in n.incoming, R(m)<=R(n)-(m,n).delta. The total weighted edge
 * lengths shall be no greater than is necessary to meet this requirement for
 * all edges in the graph.
 * <LI>attempt to order the nodes in their ranks as to minimize crossings.
 * <LI>assign <em>y</em> coordinates to each node based on its rank. The spacing
 * between ranks is the sum of the bottom padding of the previous rank, and the
 * top padding of the next rank.
 * <LI>assign <em>x</em> coordinates such that the graph is easily readable. The
 * exact behavior is undefined, but will favor edge's with higher
 * {@link Edge#weight}s. The minimum x value assigned to a node or bendpoint
 * will be 0.
 * <LI>assign <em>bendpoints</em> to all edge's which span more than 1 rank.
 * </UL>
 * <P>
 * For each NODE:
 * <UL>
 * <LI>The x coordinate will be assigned a value >= 0
 * <LI>The y coordinate will be assigned a value >= 0
 * <LI>The rank will be assigned a value >=0
 * <LI>The height will be set to the height of the tallest node on the same row
 * </UL>
 * <P>
 * For each EDGE:
 * <UL>
 * <LI>If an edge spans more than 1 row, it will have a list of
 * {@link org.eclipse.draw2d.graph.Edge#vNodes virtual} nodes. The virtual nodes
 * will be assigned an x coordinate indicating the routing path for that edge.
 * <LI>If an edge is a feedback edge, it's <code>isFeedback</code> flag will be
 * set, and if it has virtual nodes, they will be in reverse order (bottom-up).
 * </UL>
 * <P>
 * This class is not guaranteed to produce the same results for each invocation.
 * 
 * @author Randy Hudson
 * @since 2.1.2
 */
public class DirectedGraphLayout implements Layout {

    List<GraphVisitor> steps = new ArrayList<GraphVisitor>();

    public DirectedGraphLayout() {
        steps.add(new TransposeMetrics());
        steps.add(new BreakCycles());
        steps.add(new RouteEdges());
        steps.add(new InitialRankSolver());
        steps.add(new TightSpanningTreeSolver());
        steps.add(new RankAssignmentSolver());
        steps.add(new PopulateRanks());
        steps.add(new VerticalPlacement());
        steps.add(new MinCross());
        steps.add(new LocalOptimizer());
        steps.add(new HorizontalPlacement());
        steps.add(new PositionUpdate());
        steps.add(new NodeShiftStep());
        steps.add(new SubgraphRankSolver());
        steps.add(new ComputeSplines());
    }

    /**
     * Lays out the given graph
     * 
     * @param graph the graph to layout
     */
    public void visit(DirectedGraph graph) {
        if (!graph.getNodes().isEmpty()) {
            layoutSubgraphs(graph);

            for (int i = 0; i < steps.size(); i++) {
                GraphVisitor visitor = steps.get(i);
                visitor.visit(graph);
            }
            for (int i = steps.size() - 1; i >= 0; i--) {
                GraphVisitor visitor = steps.get(i);
                visitor.revisit(graph);
            }

            // System.out.println();
            // System.out.println("height: " + graph.getLayoutSize().height);
            // System.out.println("width: " + graph.getLayoutSize().width);
        }
    }

    private void layoutSubgraphs(DirectedGraph graph) {
        for (Node node : graph.getNodes()) {
            if (node instanceof Subgraph) {
                Subgraph subgraph = (Subgraph) node;
                if (subgraph.isCluster()
                        && "false".equals(subgraph.getProperties().get(
                                "collapsed"))) {
                    DirectedGraph directedSubgraph = new DirectedGraph();
                    directedSubgraph.setNodes(subgraph.getMembers());
                    directedSubgraph.setProperties(subgraph.getProperties());
                    // directedSubgraph.setSubgraph(subgraph);
                    directedSubgraph.setEdges(cullEdges(graph.getEdges(),
                            directedSubgraph));

                    visit(directedSubgraph);

                    // System.out.println(subgraph.getData());
                    // System.out.println("height: " + subgraph.getHeight()
                    // + " / " + directedSubgraph.getLayoutSize().height);
                    // System.out.println("width: " + subgraph.getWidth() +
                    // " / "
                    // + directedSubgraph.getLayoutSize().width);
                    // FIXME unconnected nodes are a pain in the hiney
                    subgraph.setSize(directedSubgraph.getLayoutSize());

                    for (Edge subEdge : directedSubgraph.getEdges()) {
                        writeEdge(graph, subEdge);
                    }
                }
            }
        }
    }

    private void writeEdge(DirectedGraph graph, Edge subEdge) {
        int id = subEdge.getId();
        for (Edge graphEdge : graph.getEdges()) {
            if (graphEdge.getId() == id) {
                graphEdge.setSpline(subEdge.getSpline());
                return;
            }
        }
    }

    private EdgeList cullEdges(EdgeList edges, DirectedGraph directedSubgraph) {
        EdgeList subEdges = new EdgeList();
        NodeList nodes = directedSubgraph.getNodes();

        for (Edge edge : edges) {
            if (isInSubgraph(nodes, edge)) {
                subEdges.add(edge);
            }
        }

        return subEdges;
    }

    private boolean isInSubgraph(NodeList nodes, Edge edge) {
        if (nodes.getNodeById(edge.getSource().getId()) == null) {
            return false;
        }
        if (nodes.getNodeById(edge.getTarget().getId()) == null) {
            return false;
        }
        return true;
    }
}
