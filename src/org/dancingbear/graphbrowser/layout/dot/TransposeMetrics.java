/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Transposer;

class TransposeMetrics extends GraphVisitor {

    Transposer t = new Transposer();

    public void revisit(DirectedGraph g) {
        if (g.getDirection() == PositionConstants.SOUTH)
            return;
        int temp;
        g.setDefaultPadding(t.t(g.getDefaultPadding()));
        for (int i = 0; i < g.getNodes().size(); i++) {
            Node node = g.getNodes().get(i);
            temp = node.getWidth();
            node.setWidth(node.getHeight());
            node.setHeight(temp);
            temp = node.getY();
            node.setY(node.getX());
            node.setX(temp);
            if (node.getPadding() != null)
                node.setPadding(t.t(node.getPadding()));
        }
        for (int i = 0; i < g.getEdges().size(); i++) {
            Edge edge = g.getEdges().getEdge(i);
            edge.getStart().transpose();

            edge.setEnd(edge.getEnd().transpose());

            edge.getPoints().transpose();
            NodeList bends = edge.getVNodes();
            if (bends == null)
                continue;
            for (int b = 0; b < bends.size(); b++) {
                Node vnode = bends.get(b);
                temp = vnode.getY();
                vnode.setY(vnode.getX());
                vnode.setX(temp);
                temp = vnode.getWidth();
                vnode.setWidth(vnode.getHeight());
                vnode.setHeight(temp);
            }
        }
        g.getSize().transpose();
    }

    public void visit(DirectedGraph g) {
        if (g.getDirection() == PositionConstants.SOUTH)
            return;
        t.setEnabled(true);
        int temp;
        g.setDefaultPadding(t.t(g.getDefaultPadding()));
        for (int i = 0; i < g.getNodes().size(); i++) {
            Node node = g.getNodes().getNode(i);
            temp = node.getWidth();
            node.setWidth(node.getHeight());
            node.setHeight(temp);
            if (node.getPadding() != null)
                node.setPadding(t.t(node.getPadding()));
        }
    }

}
