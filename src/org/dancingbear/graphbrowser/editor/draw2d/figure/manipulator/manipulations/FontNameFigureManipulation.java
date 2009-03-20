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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * Manipulate front name
 * 
 * @author Jeroen van Schagen
 * @date 26-02-2009
 */
public class FontNameFigureManipulation implements IFigureManipulation {

    public boolean manipulateFigure(IFigure figure, String value) {
        if (figure == null) {
            return false;
        }

        if (figure.getFont() != null) {
            FontData data = figure.getFont().getFontData()[0];
            data.setName(value);

            Font font = new Font(figure.getFont().getDevice(), data);
            figure.setFont(font);
        }

        return false;
    }

}