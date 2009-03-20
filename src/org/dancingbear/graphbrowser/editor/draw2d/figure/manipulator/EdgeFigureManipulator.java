/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator;

import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.manipulations.EdgeArrowManipulation;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.manipulations.ForeGroundColorFigureManipulation;

/**
 * Manipulator of edges
 * 
 * @author Michel de Graaf
 * @author Jeroen van Schagen
 */
class EdgeFigureManipulator extends AbstractFigureManipulator {

    public EdgeFigureManipulator() {
        addManipulator("color", new ForeGroundColorFigureManipulation());
        addManipulator("arrowhead", new EdgeArrowManipulation());
    }

}