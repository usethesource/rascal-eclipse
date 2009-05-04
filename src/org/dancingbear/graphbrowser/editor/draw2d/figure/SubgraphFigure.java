/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d.figure;

import org.dancingbear.graphbrowser.editor.draw2d.CollapseButton;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

/**
 * Subgraph visual representation
 * 
 * @author Jeroen van Schagen
 * @author Jeroen Bach
 * @author Taco Witte
 */
public class SubgraphFigure extends RectangleFigure {

    private Label labelName;
    private CollapseButton button;

    public SubgraphFigure() {
        setLayoutManager(new XYLayout());

        labelName = new Label();
        add(labelName, ToolbarLayout.ALIGN_CENTER);
        setConstraint(labelName, new Rectangle(20, 2, -1, -1));

        button = new CollapseButton();
        add(button);
        setConstraint(button, new Rectangle(2, 2, 10, 10));

        setBorder(new LineBorder(1));
        setOpaque(true);
    }

    /**
     * Paint SubGraph figure
     * 
     * @param graphics Graphics to paint
     */
    @Override
    public void paint(Graphics graphics) {
        graphics.setAntialias(SWT.ON);

        super.paint(graphics);
    }

    /**
     * Adds the given listener to the list of action listeners of this
     * buttonfigure. Listener is called whenever an action is performed.
     * 
     * @param listener The ActionListener to be added
     * @since 2.0
     */
    public void addActionListener(ActionListener listener) {
        button.addActionListener(listener);
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
    }

    /**
     * Change text
     * 
     * @param text Text
     */
    public void setText(String text) {
        labelName.setText(text);
    }

    /**
     * Get text
     * 
     */
    public String getText() {
        return labelName.getText();
    }

    /**
     * Get state of collapsed
     * 
     * @return isCollapsed is subgraph collapsed
     */
    public boolean isCollapsed() {
        return button.isCollapsed();
    }

    /**
     * Set subgraph collapsed state
     * 
     * @param collapsed State of collapsed
     */
    public void setCollapsed(boolean collapsed) {
        button.setCollapsed(collapsed);
    }

    /**
     * Collapse subgraph
     */
    public void doCollapse() {
        button.doClick();
    }

}