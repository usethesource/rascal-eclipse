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
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Representation of a Circle.
 * 
 * @author Ka-Sing Chou
 * @date 16-03-2009
 */
public class CircleShape extends Shape {
    /**
     * @see Shape#fillShape(Graphics)
     */
    @Override
    protected void fillShape(Graphics graphics) {
        graphics.fillOval(calculateSquarePoints());
    }

    /**
     * @see Shape#outlineShape(Graphics)
     */
    @Override
    protected void outlineShape(Graphics graphics) {
        graphics.drawOval(calculateSquarePoints());
    }

    /**
     * Retrieve points of a square, based on current boundaries. It draws the
     * circle in middle of the selection square.
     * 
     * @return Rectangle
     */
    private Rectangle calculateSquarePoints() {
        int width = getSize().width;
        int height = getSize().height;
        boolean isWidthMin = width <= height ? true : false;
        int min_borderSize = width <= height ? width : height;
        int x_location = getLocation().x;
        int y_location = getLocation().y;
        if (isWidthMin) {
            y_location = (height - width) / 2 + y_location;
        } else {
            x_location = (width - height) / 2 + x_location;
        }
        Rectangle squareBounds = new Rectangle(x_location, y_location,
                min_borderSize, min_borderSize);
        return squareBounds;
    }
}
