/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure;

import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.DefaultShape;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Shape;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Label figure with shape decoration behind the text. This class allows users
 * to change the shape of a figure during runtime.
 * 
 * @see Label
 * 
 * @author Jeroen van Schagen
 * @author Ka-Sing Chou
 * @date 06-03-2009
 */
public abstract class AbstractShapedLabel extends Label {

    private Shape shape = new DefaultShape();

    /**
     * Get background color of shape
     * 
     * @return color Background color
     */
    public Color getBackgroundColor() {
        return shape.getBackgroundColor();
    }

    /**
     * Set bounds of shape
     * 
     * @param rect Rectangle with bounds
     */
    public void setBounds(Rectangle rect) {

        shape.setBounds(getEfficientRectangle(rect));
        super.setBounds(rect);
    }

    /**
     * Retrieve new rectangle where the bounds are fixed by rounding up
     * 
     * @return Rectangle
     */
    private Rectangle getEfficientRectangle(Rectangle rect) {
        final int width = rect.width - 1;
        final int height = rect.height - 1;
        return new Rectangle(rect.x, rect.y, width, height);
    }

    /**
     * Retrieve the figures shape.
     * 
     * @return shape
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * Get foregroundcolor of shape
     * 
     * @return foregroundColor Foreground color of shape
     */
    @Override
    public Color getForegroundColor() {
        return shape.getForegroundColor();
    }

    /**
     * Paint the label and its shape, with anti-aliasing on.
     * 
     * @param graphics Graphics
     * @see Figure#paint(Graphics)
     */
    public void paint(Graphics graphics) {
        graphics.setAntialias(SWT.ON);

        shape.paint(graphics);
        super.paint(graphics);
    }

    /**
     * @see Figure#setBackgroundColor(Color)
     */
    public void setBackgroundColor(Color bg) {
        shape.setBackgroundColor(bg);
    }

    /**
     * Change the figures shape.
     * 
     * @param shape Shape figure, which cannot be null
     * @return success
     */
    public boolean setShape(Shape shape) {
        IFigure previousContent = this.shape;

        if (shape != null) {
            this.shape = shape;

            // Adjust shape bounds to label
            shape.setBounds(getBounds());

            if (previousContent != null) {
                // Inherit previous content properties
                setBackgroundColor(previousContent.getBackgroundColor());
                setForegroundColor(previousContent.getForegroundColor());
            }

            repaint();
            return true;
        }

        return false;
    }

    /**
     * @see Figure#setForegroundColor(Color)
     */
    public void setForegroundColor(Color fg) {
        shape.setForegroundColor(fg);
    }

    /**
     * Change the parent constraints and shape boundaries.
     * 
     * @param constraint Parent constraint
     */
    public void setParentConstraint(Rectangle constraint) {
        if (getParent() != null) {
            getParent().setConstraint(this, constraint);
        }

        shape.setBounds(constraint);
    }

}