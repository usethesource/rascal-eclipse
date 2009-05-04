/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.model;

import java.util.List;

/**
 * Curve abstraction
 * 
 * @author Jeroen van Schagen
 * @date 18-02-2009
 */
public interface ICurve {

    /**
     * Change the direction vector for specified index.
     * 
     * @param index Index of the vector
     * @param vector New vector value
     * @return success
     */
    boolean setDirectionVector(int index, Position vector);

    /**
     * Retrieve direction vector for specified index.
     * 
     * @param index Index of the vector to return
     * @return vector Vector at the specified position
     * @throws IndexOutOfBoundsException When index is out of range
     */
    Position getDirectionVector(int index) throws IndexOutOfBoundsException;

    /**
     * Retrieve all direction vectors of the curve. The amount of vectors can
     * vary per implementation.
     * 
     * @return vectors
     */
    List<Position> getDirectionVectors();

    /**
     * Retrieve source position of the curve.
     * 
     * @return sourcePosition
     */
    Position getSourcePosition();

    /**
     * Retrieve target position of the curve.
     * 
     * @return targetPosition
     */
    Position getTargetPosition();

}