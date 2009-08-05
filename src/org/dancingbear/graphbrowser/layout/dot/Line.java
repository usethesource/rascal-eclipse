/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.layout.dot;


public class Line {

    private PointDouble startLine;
    private PointDouble endLine;

    public Line(PointDouble start, PointDouble end) {
        this.startLine = start;
        this.endLine = end;
    }

    /**
     * Get the startpoint for this line
     * 
     * @return startpoint
     */
    public PointDouble getStart() {
        return startLine;
    }

    /**
     * Set the startpoint for this line
     */
    protected void setStart(PointDouble start) {
        this.startLine = start;
    }

    /**
     * Get the endpoint for this line
     * 
     * @return endpoint
     */
    public PointDouble getEnd() {
        return endLine;
    }

    /**
     * set the endpoint for this line
     */
    protected void setEnd(PointDouble end) {
        this.endLine = end;
    }

    /**
     * Get the position of the Line at some y coordinate
     * 
     * @return the requested coordinate
     */
    public PointDouble getPositionAt(double yValue) {
        if (yValue < startLine.y || yValue > endLine.y) {
            return null;
        }
        double dx = endLine.x - startLine.x;
        double dy = endLine.y - startLine.y;
        double factor = (yValue - startLine.y) / dy;

        return new PointDouble(startLine.x + dx * factor, yValue);
    }

    /**
     * Calculate the distance to a Horizontal Line
     * 
     * @param otherLine the line to compute the distance too
     * @return the distace
     */
    public double getDistanceToHorizontalLine(HorizontalLine otherLine) {
        PointDouble point = getPositionAt(otherLine.getY());
        if (point != null) {
            double xPos = point.getX();
            double start = otherLine.getStart().x;
            double end = otherLine.getEnd().x;

            if (xPos > start && xPos < end) {
                return 0.0;
            }

            if (xPos < start) {
                return start - xPos;
            }
            return end - xPos;

        }
        System.err.println("X not at line!");
        return 0.0;

    }

    /**
     * Get the line's length
     * 
     * @return the length of the line
     */
    public double getLength() {
        double x = endLine.x - startLine.x;
        double y = endLine.y - startLine.y;

        double length = Math.sqrt(x * x + y * y);

        return length;
    }

}
