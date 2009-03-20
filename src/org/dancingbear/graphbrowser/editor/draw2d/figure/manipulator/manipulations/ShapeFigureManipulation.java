/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.manipulations;

import org.dancingbear.graphbrowser.editor.draw2d.figure.AbstractShapedLabel;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.IFigureManipulation;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Shape;

/**
 * Manipulate shape
 * 
 * @author Jeroen van Schagen
 * @date 06-03-2009
 */
public class ShapeFigureManipulation implements IFigureManipulation {

    public boolean manipulateFigure(IFigure figure, String value) {
        if (figure instanceof AbstractShapedLabel) {
            try {
                AbstractShapedLabel container = (AbstractShapedLabel) figure;
                Shape shape = ShapeParser.getDefault().parseShape(value);
                return container.setShape(shape);
            } catch (IllegalArgumentException e) {
                // Invalid shape value, request ignored
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

}