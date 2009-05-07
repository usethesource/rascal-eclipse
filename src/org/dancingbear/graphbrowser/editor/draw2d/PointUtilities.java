/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d;

import org.dancingbear.graphbrowser.model.Position;
import org.eclipse.draw2d.geometry.Point;

/**
 * This class provides utility methods for conversion of points to positions and
 * vice versa
 * 
 * @author Jeroen van Schagen
 * 
 */
public class PointUtilities {

    /**
     * Convert position to point.
     * 
     * @param position Position
     * @return point
     */
    public static Point toPoint(Position position) {
        if (position == null) {
            return null;
        }
        return new Point(position.getX(), position.getY());
    }

    /**
     * Convert point to position.
     * 
     * @param point Point
     * @return position
     */
    public static Position toPosition(Point point) {
        if (point == null) {
            return null;
        }
        return new Position(point.preciseX(), point.preciseY());
    }

}