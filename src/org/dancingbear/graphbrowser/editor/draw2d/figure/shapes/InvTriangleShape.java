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
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Representation of a inverted triangle.
 * 
 * @author Ka-Sing Chou
 * @date 10-03-2009
 */
public class InvTriangleShape extends Shape {

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
     * Retrieve points of inverted triangle, based on current boundaries.
     * 
     * @return points
     */
    private PointList calculatePoints() {
        final Rectangle bounds = getBounds();
        PointList invTriangle = new PointList(3);
        invTriangle.addPoint(bounds.getBottom());
        invTriangle.addPoint(bounds.getTopLeft());
        invTriangle.addPoint(bounds.getTopRight());
        return invTriangle;
    }

}