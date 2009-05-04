/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure;

import org.dancingbear.graphbrowser.editor.draw2d.router.BezierConnectionRouter;
import org.dancingbear.graphbrowser.model.Spline;
import org.eclipse.draw2d.ConnectionRouter;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.swt.SWT;

/**
 * Spline visual representation
 * 
 * @author Jeroen van Schagen
 * @date 18-02-2009
 */
public class SplineFigure extends PolylineConnection {

    public SplineFigure() {
        setConnectionRouter(new BezierConnectionRouter());
    }

    /**
     * Paint SplineFigure
     * 
     * @param graphics Graphics to paint
     */
    @Override
    public void paint(Graphics graphics) {
        graphics.setAntialias(SWT.ON);
        super.paint(graphics);
    }

    /**
     * Disable bezier router and draw straight line TODO: Should recalculate
     * bezier
     */
    public void resetConnectionRouter() {
        setConnectionRouter(ConnectionRouter.NULL);
    }

    /**
     * Set Spline
     * 
     * @param spline Spline
     */
    public void setSpline(Spline spline) {
        setRoutingConstraint(spline);
    }

}