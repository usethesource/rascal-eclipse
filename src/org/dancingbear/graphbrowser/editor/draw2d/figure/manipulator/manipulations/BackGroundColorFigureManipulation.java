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

/**
 * Manipulate figure background color
 * 
 * @author Jeroen van Schagen
 * @author Michel de Graaf
 * @date 18-02-2009
 */
public class BackGroundColorFigureManipulation implements IFigureManipulation {

    public boolean manipulateFigure(IFigure figure, String value) {
        if (figure != null) {
            figure.setBackgroundColor(ColorParser.getColor(value));
            return true;
        }
        return false;
    }

}