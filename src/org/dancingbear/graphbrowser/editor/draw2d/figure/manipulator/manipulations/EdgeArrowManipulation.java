/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.manipulations;

import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.IFigureManipulation;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RotatableDecoration;

/**
 * Manipulate arrowheads
 * 
 * @author Ka-Sing Chou
 * @date 13-03-2009
 */
public class EdgeArrowManipulation implements IFigureManipulation {

    public boolean manipulateFigure(IFigure figure, String value) {
        if (figure instanceof PolylineConnection) {
            PolylineConnection connection = (PolylineConnection) figure;
            connection.setTargetDecoration(getDecoration(value));
        }
        return false;
    }

    /**
     * Based on the input, the decoration of the arrowhead will be identified
     * and returned
     * 
     * @param value The name of the arrowhead
     * @return The decoration of the arrowhead
     */
    private RotatableDecoration getDecoration(String value) {
        PolygonDecoration decoration = new PolygonDecoration();
        decoration.setScale(15, 5);
        if (value.equals("normal")) {
            decoration.setTemplate(PolygonDecoration.TRIANGLE_TIP);
        } else if (value.equals("inv")) {
            decoration.setTemplate(PolygonDecoration.INVERTED_TRIANGLE_TIP);
        } else if (value.equals("dot")) {
        } else if (value.equals("odot")) {
        } else if (value.equals("invdot")) {
        } else if (value.equals("invodot")) {
        } else if (value.equals("none")) {
            decoration.setScale(0, 0);
        } else {
            return null;
        }
        return decoration;
    }
}
