/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.model;

import org.dancingbear.graphbrowser.layout.PointDouble;

/**
 * Position model, used to represent a location in a two-dimensional space.
 * 
 * @author Jeroen van Schagen
 * @date 05-02-2009
 */
public class Position {

    private double x;
    private double y;

    /**
     * Default constructor, which initiates at position {0,0}
     */
    public Position() {
        this(0.0, 0.0);
    }

    /**
     * Initiate position on the specified x and y.
     * 
     * @param x Coordinate on x-axis
     * @param y Coordinate on y-axis
     */
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Position(PointDouble point) {
        if (point != null) {
            x = point.getX();
            y = point.getY();
        } else {
            // NOTE: is this desirable?
            x = .0;
            y = .0;
        }
    }

    /**
     * Retrieve x coordinate
     * 
     * @return Coordinate on x-axis
     */
    public double getX() {
        return x;
    }

    /**
     * Retrieve y coordinate
     * 
     * @return Coordinate on y-axis
     */
    public double getY() {
        return y;
    }

    /**
     * Change x coordinate
     * 
     * @param x Coordinate on x-axis
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Change y coordinate
     * 
     * @param y Coordinate on y-axis
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Multiply the x,y coordinates according to the specified scale.
     * 
     * @param scale Scale
     * @return Scaled position
     */
    public Position scale(double scale) {
        double xCoordinate = this.getX() * scale;
        double yCoordinate = this.getY() * scale;
        return new Position(xCoordinate, yCoordinate);
    }

    /**
     * Combine the x,y coordinates of two positions.
     * 
     * @param point Position to be added
     * @return Combined position
     */
    public Position translate(Position point) {
        double xCoordinate = this.getX() + point.getX();
        double yCoordinate = this.getY() + point.getY();
        return new Position(xCoordinate, yCoordinate);
    }

    @Override
    public String toString() {
        return "X: " + x + ", Y: " + y;
    }

}