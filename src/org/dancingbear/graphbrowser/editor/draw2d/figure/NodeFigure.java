/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure;

import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.RectangleShape;

/**
 * Node visual representation.
 * 
 * @author Jeroen van Schagen
 * @date 06-03-2009
 */
public class NodeFigure extends AbstractShapedLabel {

    /**
     * Initiate node figure as rectangle.
     */
    public NodeFigure() {
        setShape(new RectangleShape());
    }

}