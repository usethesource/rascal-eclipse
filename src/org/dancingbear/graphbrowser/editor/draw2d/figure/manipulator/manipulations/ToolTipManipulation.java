/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.manipulations;

import org.dancingbear.graphbrowser.editor.draw2d.figure.NodeFigure;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.IFigureManipulation;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

/**
 * Manipulate tooltip
 * 
 * @author Jeroen van Lieshout
 * 
 */
public class ToolTipManipulation implements IFigureManipulation {

    public boolean manipulateFigure(IFigure figure, String value) {
        if (figure instanceof NodeFigure) {
            NodeFigure node = (NodeFigure) figure;
            String toolTipText = value;

            // If no comment exists then use name as tooltip text
            if ("".equals(toolTipText)) {
                toolTipText = node.getText();
            }

            // create label figure for tooltip
            Label toolTip = new Label(toolTipText);
            node.setToolTip(toolTip);
            return true;
        }
        return false;
    }
}
