/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.layout.dot;


/**
 * @author Alex Hartog, Lars de Ridder
 * 
 */
public class HorizontalLine extends Line {

    /**
     * 
     * @param start The start point of the horizontal line
     * @param endX The end point of the horizontal line
     */
    public HorizontalLine(PointDouble start, double endX) {
        super(start, new PointDouble(endX, start.y));
    }

    /**
     * @return The y coordinate of the horizontal line
     */
    public double getY() {
        return getStart().y;
    }

}
