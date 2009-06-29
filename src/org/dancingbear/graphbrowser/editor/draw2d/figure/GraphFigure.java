/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LayoutAnimator;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;

/**
 * Visual representation of a graph
 * 
 * @author Jeroen van Schagen
 * @date 19-02-2009
 */
public class GraphFigure extends Figure {

    public GraphFigure() {
        setLayoutManager(new XYLayout());
        addLayoutListener(LayoutAnimator.getDefault());
    }

    /**
     * Paint GraphFigure
     * 
     * @param graphics Graphics to paint
     */
    @Override
    public void paint(Graphics graphics) {
        graphics.setAntialias(SWT.ON);
        super.paint(graphics);
    }
}