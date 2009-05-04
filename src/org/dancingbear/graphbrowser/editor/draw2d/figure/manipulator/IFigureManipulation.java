/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator;

import org.eclipse.draw2d.IFigure;

/**
 * Figure manipulations, allow the manipulation of figure implementations.
 * 
 * @author Jeroen van Schagen
 * @date 18-02-2009
 */
public interface IFigureManipulation {

    /**
     * Manipulate a figure
     * 
     * @param figure Figure to be manipulated
     * @param value Value of manipulation
     * @return success Manipulation success
     */
    public boolean manipulateFigure(IFigure figure, String value);

}