/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class NodeShiftStep extends GraphVisitor {

    @Override
    void visit(DirectedGraph g) {

        ArrayList<Node> leftToRight = new ArrayList<Node>();
        leftToRight.addAll(g.getNodes());

        Collections.sort(leftToRight, new Comparator<Node>() {
            public int compare(Node node1, Node node2) {
                int result = node1.getX() - node2.getX();
                if (result == 0) {
                    result += node1.getY() - node2.getY();
                }
                return result;
            }
        });

        for (int rankNo = 0; rankNo < g.getRanks().size(); rankNo++) {
            Rank rank = g.getRanks().getRank(rankNo);
            for (Node node : rank) {
                if (isIllegallyInside(node, g)) {
                    pushNodes(node, g);
                }
            }
        }

    }

    private void pushNodes(Node left, DirectedGraph g) {
        Node right = left.getRight();
        int pushAmount = getRightSide(left, g) - getLeftSide(right, g);

        shiftGraph(right.getX(), pushAmount, g);
    }

    private void shiftGraph(int from, int shift, DirectedGraph g) {
        for (Node node : g.getNodes()) {
            if (node.getX() >= from) {
                node.setX(node.getX() + shift);
            }
        }
        g.getSize().width += shift;
    }

    private int getLeftSide(Node node, DirectedGraph g) {
        return node.getX() - g.getPadding(node).left;
    }

    private int getRightSide(Node node, DirectedGraph g) {
        return node.getX() + node.getWidth() + g.getPadding(node).right;
    }

    private boolean isIllegallyInside(Node left, DirectedGraph g) {
        Node right = left.getRight();
        if (right == null || areBothVirtual(left, right)) {
            return false;
        }

        return isInside(left, right, g);
    }

    private boolean isInside(Node left, Node right, DirectedGraph g) {
        return getLeftSide(right, g) < getRightSide(left, g);
    }

    private boolean areBothVirtual(Node left, Node right) {
        return left.isVirtualNode() && right.isVirtualNode();
    }
}
