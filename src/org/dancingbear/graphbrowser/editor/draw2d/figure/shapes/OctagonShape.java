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
 * Representation of a Octagon.
 * 
 * @author Ka-Sing Chou
 * @date 10-03-2009
 */
public class OctagonShape extends Shape {

    private final static double RATIO_X = 3.0;
    private final static double RATIO_Y = 3.0;

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
     * Retrieve points of octagon, based on current boundaries.
     * 
     * @return points
     */
    private PointList calculatePoints() {
        final Rectangle bounds = getBounds();
        final Dimension distanceX = getSize().getScaled(1.0 / RATIO_X);
        final Dimension distanceY = getSize().getScaled(1.0 / RATIO_Y);

        PointList octagon = new PointList(8);
        octagon.addPoint(bounds.getTopLeft().translate(distanceX.width, 0));
        octagon.addPoint(bounds.getTopRight().translate(-distanceX.width, 0));
        octagon.addPoint(bounds.getTopRight().translate(0, distanceY.height));
        octagon.addPoint(bounds.getBottomRight()
                .translate(0, -distanceY.height));
        octagon
                .addPoint(bounds.getBottomRight()
                        .translate(-distanceX.width, 0));
        octagon.addPoint(bounds.getBottomLeft().translate(distanceX.width, 0));
        octagon
                .addPoint(bounds.getBottomLeft()
                        .translate(0, -distanceY.height));
        octagon.addPoint(bounds.getTopLeft().translate(0, distanceY.height));
        return octagon;
    }

}
