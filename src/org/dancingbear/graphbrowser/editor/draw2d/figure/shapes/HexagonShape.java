/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure.shapes;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Representation of a Hexagon(Polygon).
 * 
 * @author Ka-Sing Chou
 * @date 10-03-2009
 */
public class HexagonShape extends Shape {

    private final static double RATIO_X = 4.0;

    /**
     * @see Shape#fillShape(Graphics)
     */
    @Override
    protected void fillShape(Graphics graphics) {
        final PointList points = calculatePoints();
        graphics.fillPolygon(points);
    }

    /**
     * @see Shape#outlineShape(Graphics)
     */
    @Override
    protected void outlineShape(Graphics graphics) {
        final PointList points = calculatePoints();
        graphics.drawPolygon(points);
    }

    /**
     * Retrieve points of Hexagon, based on current boundaries.
     * 
     * @return points
     */
    private PointList calculatePoints() {
        final Rectangle bounds = getBounds();
        final Dimension distance = getSize().getScaled(1.0 / RATIO_X);

        PointList hexagon = new PointList(6);
        hexagon.addPoint(bounds.getTopLeft().translate(distance.width, 0));
        hexagon.addPoint(bounds.getTopRight().translate(-distance.width, 0));
        hexagon.addPoint(bounds.getRight());
        hexagon.addPoint(bounds.getBottomRight().translate(-distance.width, 0));
        hexagon.addPoint(bounds.getBottomLeft().translate(distance.width, 0));
        hexagon.addPoint(bounds.getLeft());

        return hexagon;
    }

}
