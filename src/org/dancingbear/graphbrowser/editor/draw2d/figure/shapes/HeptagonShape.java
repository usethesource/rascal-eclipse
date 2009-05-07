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
 * Representation of a (Heptagon)Polygon.
 * 
 * @author Ka-Sing Chou
 * @date 10-03-2009
 */
public class HeptagonShape extends Shape {

    private final static double RATIO_X = 8.0;
    private final static double RATIO_Y = 5.0;

    /**
     * @see Shape#fillShape(Graphics)
     */
    @Override
    protected void fillShape(Graphics graphics) {
        PointList points = calculatePoints();
        graphics.fillPolygon(points);
    }

    /**
     * @see Shape#outlineShape(Graphics)
     */
    @Override
    protected void outlineShape(Graphics graphics) {
        PointList points = calculatePoints();
        graphics.drawPolygon(points);
    }

    /**
     * Retrieve points of heptagon , based on current boundaries.
     * 
     * @return points
     */
    private PointList calculatePoints() {
        Rectangle bounds = getBounds();
        Dimension distanceX = getSize().getScaled(1.0 / RATIO_X);
        Dimension distanceY = getSize().getScaled(1.0 / RATIO_Y);

        PointList heptagon = new PointList(7);
        heptagon.addPoint(bounds.getTop());
        heptagon.addPoint(bounds.getTopRight().translate(-distanceX.width,
                distanceY.height));
        heptagon.addPoint(bounds.getBottomRight().translate(0,
                2 * -distanceY.height));
        heptagon.addPoint(bounds.getBottomRight().translate(
                2 * -distanceX.width, 0));
        heptagon.addPoint(bounds.getBottomLeft().translate(2 * distanceX.width,
                0));
        heptagon.addPoint(bounds.getBottomLeft().translate(0,
                2 * -distanceY.height));
        heptagon.addPoint(bounds.getTopLeft().translate(distanceX.width,
                distanceY.height));

        return heptagon;
    }

}
