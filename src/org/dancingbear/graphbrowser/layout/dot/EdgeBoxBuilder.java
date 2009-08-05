/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.layout.dot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.dancingbear.graphbrowser.layout.model.DirectedGraph;
import org.dancingbear.graphbrowser.layout.model.Node;
import org.eclipse.draw2d.geometry.Insets;

/**
 * An edgebox builder creates and builds a list of all boxes a full edge may
 * travel through according to the dot layout algorithm
 * 
 * @author Joppe
 * @version $Id$
 */
public class EdgeBoxBuilder {
    private final static int NUM_BOXES_PER_LINE = 4;

    private DirectedGraph directedgraph;
    private ArrayList<Box> boxes;

    /**
     * Constructor for the EdgeBoxBuilder
     * 
     * @param graph The graph the edge for these boxes belongs to
     */
    public EdgeBoxBuilder(DirectedGraph graph) {
        directedgraph = graph;
        boxes = new ArrayList<Box>();
    }

    /**
     * Add a box between source and target
     * 
     * @param source The source node
     * @param target The target node
     */
    private void addBetweenRankBoxes(Node source, Node target) {
        if (source.isVirtualNode() && target.isVirtualNode()) {
            addInterVirtualNodeBox(source, target);
        } else {
            addInterRankBoxes(source, target);
        }
    }

    /**
     * Add a box between source and target when either or both are non-virtual
     * 
     * @param source The source node
     * @param target The target node
     */
    // TODO why more than one box between a rank...why the difference?
    private void addInterRankBoxes(Node source, Node target) {
        double boxTop = source.getY() + source.getHeight();
        double boxHeight = (target.getY() - boxTop) / NUM_BOXES_PER_LINE;

        for (int boxIndex = 0; boxIndex < NUM_BOXES_PER_LINE; boxIndex++) {
            boxes.add(new Box(0, boxTop, directedgraph.getLayoutSize()
                    .preciseWidth(), boxHeight));
            boxTop += boxHeight;
        }
    }

    /**
     * Add a box between source and target when both are virtual
     * 
     * @param source The virtual source node
     * @param target The virtual target node
     */
    private void addInterVirtualNodeBox(Node source, Node target) {
        int top = source.getY() + source.getHeight();
        boxes.add(new Box(0, top, directedgraph.getLayoutSize().preciseWidth(),
                target.getY() - top));
    }

    /**
     * Add a box around the node
     * 
     * @param node The node in question
     * @param top The top of the box
     * @param height The height of the box
     */
    private void addRankBox(Node node, double top, double height) {
        double leftBound = getLeftBound(node);
        double rightBound = getRightBound(node);

        boxes.add(new Box(leftBound, top, rightBound - leftBound, height));
    }

    /**
     * Add a box around the source node and around the target node when it is
     * the end node.
     * 
     * @param source The source node
     * @param target The target node
     */
    private void addRankBoxes(Node source, Node target) {
        addSourceRankBox(source);
        if (!target.isVirtualNode()) {
            addTargetRankBox(target);
        }
    }

    /**
     * Add a box around the source node
     * 
     * @param source The source node
     */
    private void addSourceRankBox(Node source) {
        if (source.isVirtualNode()) {
            addRankBox(source, source.getY(), source.getHeight());
        } else {
            double height = source.getHeight() / 2.0;
            addRankBox(source, source.getY() + height, height);
        }
    }

    /**
     * Add a box around the target node
     * 
     * @param target The target node
     */
    private void addTargetRankBox(Node target) {
        double height = target.getHeight() / 2.0;
        double top = target.getY();

        addRankBox(target, top, height);
    }

    /**
     * Add the boxes to the list that belong to and between source and target
     * 
     * @param source The source node
     * @param target The target node
     */
    public void appendBoxesFor(Node source, Node target) {
        addRankBoxes(source, target);
        addBetweenRankBoxes(source, target);
    }

    /**
     * Retrieve what has been built
     * 
     * @return The list of boxes that has been constructed
     */
    public ArrayList<Box> getBoxes() {
        // organize boxes from top to bottom
        Collections.sort(boxes, new Comparator<Box>() {
            public int compare(Box box1, Box box2) {
                double deltaY = box1.getTopLeft().y - box2.getTopLeft().y;
                return (deltaY < 0.0) ? -1 : (deltaY > 0.0) ? 1 : 0;
            }
        });
        return boxes;
    }

    /**
     * Retrieve the right side of the node to the left of the given node from
     * where drawing is possible
     * 
     * @param node The node in question
     * @return The right side of the node to the left
     */
    private double getLeftBound(Node node) {
        Node left = getNodeToLeftOf(node);

        if (left == null) {
            return 0.0;
        }

        double minimum = node.getX() - getPadding(node).left;
        double leftNodeRightBound = left.getX() + left.getWidth()
                + getPadding(left).right;
        return Math.min(minimum, leftNodeRightBound);
    }

    /**
     * Retrieves the node to the left of the given node
     * 
     * @param node The node in question
     * @return The node to the left or null if there is none
     */
    protected Node getNodeToLeftOf(Node node) {
        return node.getLeft();
    }

    /**
     * Retrieves the node to the right of the given node
     * 
     * @param node The node in question
     * @return The node to the right or null if there is none
     */
    protected Node getNodeToRightOf(Node node) {
        return node.getRight();
    }

    /**
     * Retrieve the specific padding around the node
     * 
     * @param node The node in question
     * @return The padding for this node
     */
    private Insets getPadding(Node node) {
        Insets padding = node.getPadding();
        if (padding == null) {
            padding = directedgraph.getDefaultPadding();
        }

        return padding;
    }

    /**
     * Retrieve the left side of the node to the right of the given node from
     * where drawing is possible
     * 
     * @param node The node in question
     * @return The left side of the node to the right
     */
    private double getRightBound(Node node) {
        Node right = getNodeToRightOf(node);
        if (right == null) {
            return directedgraph.getLayoutSize().preciseWidth();
        }

        double minimum = node.getX() + node.getWidth() + getPadding(node).right;
        double rightNodeLeftBound = right.getX() - getPadding(right).left;

        return Math.max(minimum, rightNodeLeftBound);
    }
}
