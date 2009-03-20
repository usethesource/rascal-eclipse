/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.ui.parts;

import java.util.ArrayList;
import java.util.List;

import org.dancingbear.graphbrowser.editor.HighlightManager;
import org.eclipse.core.runtime.Assert;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.SelectionManager;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.viewers.ISelection;

/**
 * Graphical viewer for highlighting and scrolling
 * 
 * @author Jeroen Bach
 * 
 */
public class HighLightScrollingGraphicalViewer extends ScrollingGraphicalViewer {

    // TODO: These methods should be added to the AbstractEditPartViewer
    private HighlightManager highlightModel;

    /**
     * The raw list of highlighted editparts.
     */
    private final List<EditPart> highlighted = new ArrayList<EditPart>();

    /**
     * Constructs the viewer and calls {@link #init()}.
     */
    public HighLightScrollingGraphicalViewer() {
        setHighlightManager(HighlightManager.createDefault());
        init();
    }

    /**
     * @see EditPartViewer#setSelectionManager(SelectionManager)
     */
    public void setHighlightManager(HighlightManager model) {
        Assert.isNotNull(model);

        if (highlightModel != null) {
            highlightModel.internalUninstall();
        }

        highlightModel = model;
        model.internalInitialize(this, highlighted, new Runnable() {
            public void run() {
                fireHighlightedChanged();
            }
        });

        if (getControl() != null) {
            model.internalHookControl(getControl());
        }
    }

    /**
     * Appends an editpart to the highlited editparts and highlight it.
     */
    public void appendHighlight(EditPart editpart) {
        highlightModel.appendHighlight(editpart);
    }

    /**
     * Removes a editpart from the highlited editparts and unhighlight it.
     */
    public void unHighlight(EditPart editpart) {
        highlightModel.unHighlight(editpart);
    }

    /**
     * Unhighlights all editpart that where previously highlighted
     */
    public void unHighlightAll() {
        highlightModel.unHighlightAll();
    }

    /**
     * Returns the manager that manages the highlights of the editparts
     */
    public HighlightManager getHighlightManager() {
        return highlightModel;
    }

    /**
     * Returns the modifiable List of highlighted EditParts.
     * 
     * @return the internal list of selected editparts
     */
    protected List<?> getHighlightedEditParts() {
        return highlighted;
    }

    /**
     * Select specified editpart
     * 
     * @param editpart EditPart to select
     */
    @Override
    public void select(EditPart editpart) {
        unHighlightAll();
        super.select(editpart);
    }

    /**
     * Deselect all selected items and unhighlight it
     */
    @Override
    public void deselectAll() {
        unHighlightAll();
        super.deselectAll();
    }

    /**
     * Set new selection
     * 
     * @param newSelection Selection to set
     */
    @Override
    public void setSelection(ISelection newSelection) {
        unHighlightAll();
        super.setSelection(newSelection);
    }

    private void fireHighlightedChanged() {
        // TODO: isn't implemented (yet)
    }

}