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

import org.dancingbear.graphbrowser.layout.model.CubicBezierCurve;
import org.dancingbear.graphbrowser.layout.model.DirectedGraph;
import org.dancingbear.graphbrowser.layout.model.Edge;
import org.dancingbear.graphbrowser.layout.model.Node;
import org.dancingbear.graphbrowser.layout.model.Subgraph;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Randy Hudson
 */
class RouteEdges extends GraphVisitor {

    static void routeLongEdge(Edge edge, DirectedGraph g) {
        ShortestPathRouter router = new ShortestPathRouter();
        Path path = new Path(edge.getStart(), edge.getEnd());
        router.addPath(path);
        Rectangle o;
        Insets padding;
        for (int i = 0; i < edge.getVNodes().size(); i++) {
            Node node = edge.getVNodes().get(i);
            Node neighbor;
            if (node.getLeft() != null) {
                neighbor = node.getLeft();
                o = new Rectangle(neighbor.getX(), neighbor.getY(), neighbor
                        .getWidth(), neighbor.getHeight());
                padding = g.getPadding(neighbor);
                o.width += padding.right + padding.left;
                o.width += (edge.getPadding() * 2);
                o.x -= (padding.left + edge.getPadding());
                o.union(o.getLocation().translate(-100000, 2));
                router.addObstacle(o);
            }
            if (node.getRight() != null) {
                neighbor = node.getRight();
                o = new Rectangle(neighbor.getX(), neighbor.getY(), neighbor
                        .getWidth(), neighbor.getHeight());
                padding = g.getPadding(neighbor);
                o.width += padding.right + padding.left;
                o.width += (edge.getPadding() * 2);
                o.x -= (padding.left + edge.getPadding());
                o.union(o.getLocation().translate(100000, 2));
                router.addObstacle(o);
            }
        }
        router.setSpacing(0);
        router.solve();
        edge.setPoints(path.getPoints());
    }

    /**
     * @see GraphVisitor#visit(DirectedGraph)
     */
    public void revisit(DirectedGraph g) {
        for (int i = 0; i < g.getEdges().size(); i++) {
            Edge edge = g.getEdges().get(i);
            edge.setStart(new Point(edge.getSourceOffset()
                    + edge.getSource().getX(), edge.getSource().getY()
                    + edge.getSource().getHeight()));
            edge.setEnd(new Point(edge.getTargetOffset()
                    + edge.getTarget().getX(), edge.getTarget().getY()));

            // Move Splines
            for (CubicBezierCurve curve : edge.getSpline().getCurves()) {
                for (PointDouble point : curve.getControlPoints()) {
                    if (null != edge.getSource().getParent()) {
                        Subgraph subgraph = edge.getSource().getParent();
                        point.setLocation(point.x + subgraph.getX(), point.y
                                + subgraph.getY());
                    }
                }
            }

            if (edge.getVNodes() != null)
                routeLongEdge(edge, g);
            else {
                PointList list = new PointList();
                list.addPoint(edge.getStart());
                list.addPoint(edge.getEnd());
                edge.setPoints(list);
            }
        }
    }
}
