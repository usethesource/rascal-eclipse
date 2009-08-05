/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.dancingbear.graphbrowser.layout.dot;

import org.dancingbear.graphbrowser.layout.model.DirectedGraph;
import org.dancingbear.graphbrowser.layout.model.Edge;
import org.dancingbear.graphbrowser.layout.model.Node;
import org.dancingbear.graphbrowser.layout.model.NodeList;
import org.dancingbear.graphbrowser.layout.model.Subgraph;
import org.dancingbear.graphbrowser.layout.model.VirtualNode;
import org.eclipse.draw2d.geometry.Insets;

/**
 * Encapsulates the conversion of a long edge to multiple short edges and back.
 * 
 * @since 3.1
 */
class VirtualNodeCreation extends RevertableChange {

    private final Edge edge;
    private final DirectedGraph graph;
    private Node nodes[];
    private Edge[] edges;

    private static final int INNER_EDGE_X = 2;
    private static final int LONG_EDGE_X = 8;

    /**
     * Breaks a single edge into multiple edges containing virtual nodes.
     * 
     * @since 3.1
     * @param edge The edge to convert
     * @param graph the graph containing the edge
     */
    public VirtualNodeCreation(Edge edge, DirectedGraph graph) {
        this.edge = edge;
        this.graph = graph;

        int size = edge.getTarget().getRank() - edge.getSource().getRank() - 1;
        int offset = edge.getSource().getRank() + 1;

        Node prevNode = edge.getSource();
        Node currentNode;
        Edge currentEdge;
        nodes = new Node[size];
        edges = new Edge[size + 1];

        Insets padding = new Insets(0, edge.getPadding(), 0, edge.getPadding());

        Subgraph s = GraphUtilities.getCommonAncestor(edge.getSource(), edge
                .getTarget());

        for (int i = 0; i < size; i++) {
            nodes[i] = currentNode = new VirtualNode(
                    "Virtual" + i + ':' + edge, s); //$NON-NLS-1$
            currentNode.setWidth(edge.getWidth());
            if (s != null) {
                currentNode.setNestingIndex(s.getNestingIndex());
            }

            currentNode.setHeight(0);
            currentNode.setPadding(padding);
            currentNode.setRank(offset + i);

            RankList ranks = graph.getRanks();
            ranks.getRank(offset + i).add(currentNode);
            graph.setRanks(ranks);
            // graph.ranks.getRank(offset + i).add(currentNode);

            currentEdge = new Edge(prevNode, currentNode, 1, edge.getWeight()
                    * LONG_EDGE_X);
            if (i == 0) {
                currentEdge.setWeight(edge.getWeight() * INNER_EDGE_X);
                currentEdge.setOffsetSource(edge.getOffsetSource());
            }
            graph.addEdge(edges[i] = currentEdge);
            graph.addNode(currentNode);
            prevNode = currentNode;
        }

        currentEdge = new Edge(prevNode, edge.getTarget(), 1, edge.getWeight()
                * INNER_EDGE_X);
        currentEdge.setOffsetTarget(edge.getOffsetTarget());
        graph.addEdge(edges[edges.length - 1] = currentEdge);
        graph.removeEdge(edge);
    }

    void revert() {
        edge.setStart(edges[0].getStart());
        edge.setEnd(edges[edges.length - 1].getEnd());
        edge.setVNodes(new NodeList());
        for (int i = 0; i < edges.length; i++) {
            graph.removeEdge(edges[i]);
        }
        for (int i = 0; i < nodes.length; i++) {
            edge.addVNode(nodes[i]);
            graph.removeNode(nodes[i]);
        }
        edge.addOutgoingSource(edge);
        edge.getTarget().getIncoming().add(edge);
        edge.setSpline(edges[0].getSpline());

        graph.addEdge(edge);
    }

}
