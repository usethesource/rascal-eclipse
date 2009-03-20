/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.draw2d;

import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.MinusShape;
import org.dancingbear.graphbrowser.editor.draw2d.figure.shapes.PlusShape;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.IFigure;

/**
 * Collapse button that fires an event when pressed. You can change the
 * SetCollapsed value and depending on this value a figure is drawn on the
 * button. This button doesn't toggle automatically, the methode that catches
 * the action should toggle the button as well. This is done because the button
 * should react on external sources and is not allowed to determine its own
 * status.
 * 
 * @author jeroenbach
 * 
 */
public class CollapseButton extends Button {

    private IFigure collapsedFigure;
    private IFigure expendFigure;
    private boolean collapsed;

    /**
     * Constructs a new CollapseButton with a default plus and minus shape.
     */
    public CollapseButton() {
        this(new PlusShape(), new MinusShape());
    }

    /**
     * Constructs a CollapseButton with a passed IFigure as collapsed and
     * expended its contents.
     * 
     * @param collapsedFigure the figure that is shown when collapsed
     * @param expendFigure the figure that is shown when expend
     * @since 2.0
     */
    public CollapseButton(IFigure collapsedFigure, IFigure expendFigure) {
        this.collapsedFigure = collapsedFigure;
        this.expendFigure = expendFigure;
        this.collapsed = false;
    }

    private void changeButton(boolean collapsed) {

        IFigure changeToFigure = collapsed ? collapsedFigure : expendFigure;
        setContents(changeToFigure);
    }

    /**
     * The collapsed status.
     */
    public boolean isCollapsed() {
        return collapsed;
    }

    /**
     * Set the collapsed status.
     */
    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        changeButton(collapsed);
    }
}
