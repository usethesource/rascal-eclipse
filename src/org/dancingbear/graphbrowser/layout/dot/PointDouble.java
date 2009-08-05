/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.layout.dot;

import java.awt.geom.Point2D;

import org.eclipse.draw2d.geometry.Point;

/**
 * @author Alex Hartog, Lars de Ridder
 * 
 */
public class PointDouble extends Point2D.Double {
	private static final long serialVersionUID = 182900163409449763L;

	public PointDouble(double xCoordinate, double yCoordinate) {
        super(xCoordinate, yCoordinate);
    }

    public PointDouble add(PointDouble point) {
        double xCoordinate = this.getX() + point.getX();
        double yCoordinate = this.getY() + point.getY();
        return new PointDouble(xCoordinate, yCoordinate);
    }

    public double dotProduct(PointDouble point) {
        return this.x * point.x + this.y * point.y;
    }

    public double getDistance(PointDouble point) {
        double xCoordinate = this.x - point.x;
        double yCoordinate = this.y - point.y;
        return Math.sqrt(xCoordinate * xCoordinate + yCoordinate * yCoordinate);
    }

    public double getNorm() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    public PointDouble normalize() {
        double norm = Math.sqrt(this.getX() * this.getX() + this.getY()
                * this.getY());
        if (norm == 0.0) {
            return new PointDouble(0, 0);
        }
        return new PointDouble(this.getX() / norm, this.getY() / norm);
    }

    public PointDouble scale(double scale) {
        double xCoordinate = this.getX() * scale;
        double yCoordinate = this.getY() * scale;
        return new PointDouble(xCoordinate, yCoordinate);
    }

    public PointDouble subtract(PointDouble point) {
        double xCoordinate = this.getX() - point.getX();
        double yCoordinate = this.getY() - point.getY();

        return new PointDouble(xCoordinate, yCoordinate);
    }

    /**
     * 
     * @return A point of the type used in the rest of the algorithm.
     */
    public Point toIntPoint() {
        return new Point(this.x, this.y);
    }

    /**
     * Retrieve the mirrored point on the other side of the "mirror" that runs
     * through mirrorPoint and is perpendicular to the line through this point
     * and mirrorPoint
     * 
     * @param mirrorPoint The point this point is mirrored in
     * 
     * @return The position on the other side of the "mirror"
     */
    public PointDouble getMirroredPoint(PointDouble mirrorPoint) {
        double resultX, resultY;
        resultX = mirrorPoint.x + (mirrorPoint.x - x);
        resultY = mirrorPoint.y + (mirrorPoint.y - y);

        return new PointDouble(resultX, resultY);
    }

    /**
     * Retrieve the point that is factor to the right of this point
     * 
     * @param factor The shift amount
     * @return A new position that is factor to the right
     */
    public PointDouble translateRight(double factor) {
        return new PointDouble(x + factor, y);
    }

    /**
     * Retrieve the point that is factor to the left of this point
     * 
     * @param factor The shift amount
     * @return A new position that is factor to the left
     */
    public PointDouble translateLeft(double factor) {
        return new PointDouble(x - factor, y);
    }
}
