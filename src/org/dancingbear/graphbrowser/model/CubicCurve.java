/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Cubic bezier curves represent a curve with four values. A begin and end
 * position and two direction vectors.
 * 
 * @see "http://en.wikipedia.org/wiki/B%C3%A9zier_curve"
 * 
 * @author Jeroen van Schagen
 * @date 16-02-2009
 */
public class CubicCurve implements ICurve {

    public static final int SOURCE_VECTOR_INDEX = 0;
    public static final int TARGET_VECTOR_INDEX = 1;

    private Position sourcePosition;
    private Position sourceVector;
    private Position targetVector;
    private Position targetPosition;

    /**
     * Construct new empty CubicCurve
     */
    public CubicCurve() {
        this.sourcePosition = new Position();
        this.sourceVector = new Position();
        this.targetVector = new Position();
        this.targetPosition = new Position();
    }

    /**
     * Construct new CubicCurve with specified positions
     * 
     * @param sourcePosition The source position
     * @param sourceVector The source vector
     * @param targetVector The target vector
     * @param targetPosition The target position
     */
    public CubicCurve(Position sourcePosition, Position sourceVector,
            Position targetVector, Position targetPosition) {
        this.sourcePosition = sourcePosition;
        this.sourceVector = sourceVector;
        this.targetVector = targetVector;
        this.targetPosition = targetPosition;
    }

    public Position getDirectionVector(int index)
            throws IndexOutOfBoundsException {
        return getDirectionVectors().get(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dancingbear.graphbrowser.model.ICurve#getDirectionVectors()
     */
    public List<Position> getDirectionVectors() {
        List<Position> result = new ArrayList<Position>();
        result.add(sourceVector);
        result.add(targetVector);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dancingbear.graphbrowser.model.ICurve#getSourcePosition()
     */
    public Position getSourcePosition() {
        return sourcePosition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dancingbear.graphbrowser.model.ICurve#getTargetPosition()
     */
    public Position getTargetPosition() {
        return targetPosition;
    }

    /**
     * Set source position
     * 
     * @param sourcePosition The source position
     * @return True if position is set, false otherwise
     */
    public boolean setSourcePosition(Position sourcePosition) {
        if (sourcePosition != null) {
            this.sourcePosition = sourcePosition;
            return true;
        }

        return false;
    }

    /**
     * Set target position
     * 
     * @param targetPosition The target position
     * @return True if position is set, false otherwise
     */
    public boolean setTargetPosition(Position targetPosition) {
        if (targetPosition != null) {
            this.targetPosition = targetPosition;
            return true;
        }

        return false;
    }

    /**
     * Check if a curve is a straight line by calculating if the gradient of the
     * curve is equal at all line segments.
     * 
     * @return True if the curve is a straight line, false otherwise
     */
    public boolean isStraightLine() {
        double gradient = getGradient(sourcePosition, targetPosition);
        double gradientSourceVector = getGradient(sourcePosition, sourceVector);
        double gradientTargetVector = getGradient(sourcePosition, targetVector);

        final double delta = 0.1;

        boolean gradientsEqual = (Math.abs(gradient - gradientSourceVector) < delta)
                & (Math.abs(gradient - gradientTargetVector) < delta);

        boolean gradientsInfinity = Double.isInfinite(gradient)
                & Double.isInfinite(gradientSourceVector)
                & Double.isInfinite(gradientTargetVector);

        return gradientsEqual || gradientsInfinity;
    }

    /**
     * Calculate the gradient of a line.
     * 
     * @param source
     * @param target
     * @return The gradient of the line
     */
    private double getGradient(Position source, Position target) {
        return (round(target.getY(), 2) - round(source.getY(), 2))
                / (round(target.getX(), 2) - round(source.getX(), 2));
    }

    /**
     * Round a double to a double with a set number of decimals.
     * 
     * @param value The value to be rounded
     * @param decimals The number of decimals
     * @return The rounded value
     */
    private double round(double value, int decimals) {
        if (decimals < 1) {
            return value;
        }
        double decimalFactor = Math.pow(10, decimals);
        value *= decimalFactor;
        long roundedValue = Math.round(value);
        value = roundedValue / decimalFactor;
        return value;
    }

    /**
     * see {@link ICurve#setDirectionVector(int, Position)}
     */
    public boolean setDirectionVector(int index, Position vector) {
        if (vector == null) {
            return false;
        }

        if (index == SOURCE_VECTOR_INDEX) {
            sourceVector = vector;
            return true;
        } else if (index == TARGET_VECTOR_INDEX) {
            targetVector = vector;
            return true;
        }

        return false;
    }
}