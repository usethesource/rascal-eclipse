/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.layout.dot;


/**
 * A class to save a box.
 * 
 * @author Alex Hartog, Lars de Ridder
 * 
 */
public class Box {

    private PointDouble topLeft;
    private double boxWidth, boxHeight;
    private boolean isInterRank;

    /**
     * Create a box
     * 
     * @param xCoordinate The x coordinate of the left side of the box
     * @param yCoordinate The y coordinate of the top side of the box
     * @param width The width of the box
     * @param height The height of the box
     */
    public Box(double xCoordinate, double yCoordinate, double width,
            double height) {
        this(new PointDouble(xCoordinate, yCoordinate), width, height);
    }

    /**
     * Create a box.
     * 
     * @param topLeft The coordinate of the top left corner of the box
     * @param width The width of the box
     * @param height The height of the box
     */
    public Box(PointDouble topLeft, double width, double height) {
        this.topLeft = topLeft;
        this.boxWidth = width;
        this.boxHeight = height;
    }

    /**
     * 
     * @param xCoordinate The x coordinate of the left side of the box
     * @param yCoordinate The y coordinate of the top side of the box
     * @param width The width of the box
     * @param height The height of the box
     * @param isInterRank Whether the box represents an inter-rank.
     */
    public Box(double xCoordinate, double yCoordinate, double width,
            double height, boolean isInterRank) {
        this(new PointDouble(xCoordinate, yCoordinate), width, height,
                isInterRank);
    }

    /**
     * Create a box.
     * 
     * @param topLeft The coordinate of the top left corner of the box
     * @param width The width of the box
     * @param height The height of the box
     * @param isInterRank Whether the box represents an inter-rank.
     */
    public Box(PointDouble topLeft, double width, double height,
            boolean isInterRank) {
        this.topLeft = topLeft;
        this.boxWidth = width;
        this.boxHeight = height;
        this.isInterRank = isInterRank;
    }

    /**
     * 
     * @return The y coordinate of the bottom side of the box
     */
    public double getBottom() {
        return topLeft.y + boxHeight;
    }

    /**
     * 
     * @return The height of the box
     */
    public double getHeight() {
        return boxHeight;
    }

    /**
     * 
     * @return The x coordinate of the left side of the box
     */
    public double getLeft() {
        return topLeft.x;
    }

    /**
     * 
     * @return The x coordinate of the right side of the box
     */
    public double getRight() {
        return topLeft.x + boxWidth;
    }

    /**
     * 
     * @return The y coordinate of the top side of the box
     */
    public double getTop() {
        return topLeft.y;
    }

    /**
     * 
     * @return Coordinate of the top left corner
     */
    public PointDouble getTopLeft() {
        return topLeft;
    }

    /**
     * 
     * @return The width of the box
     */
    public double getWidth() {
        return boxWidth;
    }

    /**
     * 
     * @param height The height of the box
     */
    public void setHeight(double height) {
        this.boxHeight = height;
    }

    /**
     * 
     * @param topLeft The coordinate of the top left corner
     */
    public void setTopLeft(PointDouble topLeft) {
        this.topLeft = topLeft;
    }

    /**
     * 
     * @param width The width of the box
     */
    public void setWidth(double width) {
        this.boxWidth = width;
    }

    /**
     * 
     * @return true if the box is an inter-rank box.
     */
    public boolean isInterRank() {
        return isInterRank;
    }
}
