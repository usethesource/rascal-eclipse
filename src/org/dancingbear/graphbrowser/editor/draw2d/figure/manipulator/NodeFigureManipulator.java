/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator;

import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.manipulations.BackGroundColorFigureManipulation;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.manipulations.FontColorFigureManipulation;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.manipulations.FontSizeFigureManipulation;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.manipulations.ShapeFigureManipulation;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.manipulations.ToolTipManipulation;

/**
 * Manipulator of nodes
 * 
 * @author Michel de Graaf
 * @author Jeroen van Schagen
 */
class NodeFigureManipulator extends AbstractFigureManipulator {

    public NodeFigureManipulator() {
        addManipulator("fillcolor", new BackGroundColorFigureManipulation());
        addManipulator("fontcolor", new FontColorFigureManipulation());
        addManipulator("fontsize", new FontSizeFigureManipulation());
        addManipulator("shape", new ShapeFigureManipulation());
        addManipulator("comment", new ToolTipManipulation());
    }

}