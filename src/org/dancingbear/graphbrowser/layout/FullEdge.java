/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.layout;

import java.util.ArrayList;

/**
 * This class represents a collection of edges from a real node to a real edge
 * (real == non-virtual)
 * 
 * @author Alex Hartog, Lars de Ridder
 * 
 * @version $Id: FullEdge.java 943 2009-03-10 18:47:47Z joppe.kroon $
 */
public class FullEdge {
    private static final int ONLY_EDGE = 0;

    private final Node startNode;
    private final Node endNode;

    private final PointDouble startPosition;
    private final PointDouble endPosition;

    private ArrayList<Box> boxes;

    private ArrayList<Node> nodePath;

    /**
     * Constructor for a full edge.
     * 
     * @param edge The edge outgoing from the start node
     * @param graph The graph this full edge is a part of
     */
    public FullEdge(Edge edge, DirectedGraph graph) {
        nodePath = buildPath(edge);
        boxes = buildBoxes(graph, nodePath);

        startNode = nodePath.get(0);
        endNode = nodePath.get(nodePath.size() - 1);

        startPosition = calculateStartPosition();
        endPosition = calculateEndPosition();
    }

    /**
     * builds the boxes the edge can travel through
     * 
     * @param graph The DirectedGraph of the edge
     * @param path The path of nodes (start-virtual*-end)
     * 
     * @return A list of the boxes
     */
    private ArrayList<Box> buildBoxes(DirectedGraph graph, ArrayList<Node> path) {
        EdgeBoxBuilder ebb = new EdgeBoxBuilder(graph);
        for (int i = 0; i < path.size() - 1; i++) {
            ebb.appendBoxesFor(path.get(i), path.get(i + 1));
        }

        return ebb.getBoxes();
    }

    /**
     * Builds the path from the source node of this full edge to its eventual
     * target node
     * 
     * @param edge The first edge part of the full edge
     * @return The list of nodes that constitute the path of nodes for this full
     * edge (start-virtual*-end)
     */
    private ArrayList<Node> buildPath(Edge edge) {
        ArrayList<Node> path = new ArrayList<Node>();
        path.add(edge.getSource());

        Node target = edge.target;
        while (target.isVirtualNode()) {
            path.add(target);
            target = getNextEdgePart(target).getTarget();
        }
        path.add(target);

        return path;
    }

    /**
     * Get the next edge that is part of this full edge
     * 
     * @param source The source node of the next edge
     * @return The next edge of the full edge, or null if the target node has
     * been reached
     */
    private Edge getNextEdgePart(Node source) {
        if (source.isVirtualNode()) { // check for end point
            if (source.getOutgoing().size() != 1) { // sanity check for a very
                // paranoid individual
                throw new AssertionError(
                        "A virtual node contained an illegal amount of outgoing edges.");
            }
            return source.getOutgoing().get(ONLY_EDGE);
        }

        return null;
    }

    /**
     * The boxes the edge is allowed to travel through
     * 
     * @return The boxes of this full edge
     */
    public ArrayList<Box> getBoxes() {
        return boxes;
    }

    /**
     * Retrieve the end point of this edge
     * 
     * @return The position of the end point in double precision
     */
    public PointDouble getEdgeEndPosition() {
        return endPosition;
    }

    /**
     * Retrieve the starting point of this edge
     * 
     * @return The position of the starting point in double precision
     */
    public PointDouble getEdgeStartPosition() {
        return startPosition;
    }

    /**
     * Retrieve the target node of this full edge
     * 
     * @return The end node of the full edge
     */
    public Node getEndNode() {
        return endNode;
    }

    /**
     * Retrieve the source node of this full edge
     * 
     * @return The start node of the full edge
     */
    public Node getStartNode() {
        return startNode;
    }

    /**
     * Calculates the end position of this edge
     * 
     * @return The end position of this edge in double precision
     */
    private PointDouble calculateEndPosition() {
        Node previousToLast = nodePath.get(nodePath.size() - 2);
        return calculateLineAnchorPoint(endNode, getCenter(previousToLast));
    }

    /**
     * Calculates the start position of this edge
     * 
     * @return The start position of this edge in double precision
     */
    private PointDouble calculateStartPosition() {
        Node second = nodePath.get(1);
        return calculateLineAnchorPoint(startNode, getCenter(second));
    }

    /**
     * Calculates the intersection of the rectangle of the node and the straight
     * line from node's center to another point.
     * 
     * @param node The node on the side of the edge to be calculated
     * @param otherCenter The center of the node on the opposite end of the edge
     * @return The intersection point of the line between the two centers and
     * the rectangle of the node
     */
    private PointDouble calculateLineAnchorPoint(Node node,
            PointDouble otherCenter) {

        PointDouble nodeCenter = getCenter(node);
        if (nodeCenter.x == otherCenter.x && nodeCenter.y == otherCenter.y) {
            // left-top for a self-edge
            return new PointDouble(node.getX(), node.getY());
        }

        double dx = nodeCenter.x - otherCenter.x;
        double dy = nodeCenter.y - otherCenter.y;

        if (isAligned(dx, dy)) {
            return getAlignedNodesAnchorPoint(node, nodeCenter, dx, dy);
        }

        return nonAlignedNodesAnchorPoint(node, nodeCenter, dx, dy);
    }

    private PointDouble nonAlignedNodesAnchorPoint(Node node,
            PointDouble nodeCenter, double dx, double dy) {
        // +.|.-
        // .1|2.
        // ----- center - other
        // .3|4.
        // -.|.+

        double derivative = dy / dx;
        double halfwidth = node.getWidth() / 2.0;
        double halfheight = node.getHeight() / 2.0;

        double relativeXValue = halfheight / derivative;
        double relativeYValue = halfwidth * derivative;

        if (Math.abs(relativeXValue) < halfwidth) {
            // intersection on bottom or top
            if (dy < 0.0) { // intersection on bottom
                return new PointDouble(nodeCenter.x + relativeXValue, node
                        .getY()
                        + node.getHeight());
            }

            // intersection on top
            return new PointDouble(nodeCenter.x - relativeXValue, node.getY());
        }

        // intersection on side
        if (dx < 0.0) { // intersection on right side
            return new PointDouble(node.getX() + node.getWidth(), nodeCenter.y
                    + relativeYValue);
        }

        // intersection on left side
        return new PointDouble(node.getX(), nodeCenter.y - relativeYValue);
    }

    private boolean isAligned(double dx, double dy) {
        return dx == 0.0 || dy == 0.0;
    }

    private PointDouble getAlignedNodesAnchorPoint(Node node,
            PointDouble nodeCenter, double dx, double dy) {
        // above or below is more common case
        if (dx == 0.0) {
            if (dy < 0.0) {
                // directly below
                return new PointDouble(nodeCenter.x, node.getY()
                        + node.getHeight());
            }
            // directly above
            return new PointDouble(nodeCenter.x, node.getY());
        }

        if (dx < 0.0) {
            // directly to the right
            return new PointDouble(nodeCenter.y, node.getX() + node.getWidth());
        }
        // directly to the left
        return new PointDouble(nodeCenter.y, node.getX());
    }

    public PointDouble getStartNodeCenter() {
        return getCenter(startNode);
    }

    public PointDouble getEndNodeCenter() {
        return getCenter(endNode);
    }

    private PointDouble getCenter(Node node) {
        double centerX = node.getX() + (node.getWidth() / 2);
        double centerY = node.getY() + (node.getHeight() / 2);

        return new PointDouble(centerX, centerY);
    }
}
