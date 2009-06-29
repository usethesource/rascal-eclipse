/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.dancingbear.graphbrowser.editor;

import java.util.List;

import org.dancingbear.graphbrowser.editor.gef.editparts.HighlightEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.swt.widgets.Control;

/**
 * Manages a viewer's highlighting model. Highlighting management includes
 * representing a form of highlighting which is available to clients of a
 * viewer. The highlight manager provides the mechanism for modifying the
 * highlighted elements and any validation.
 */
public class HighlightManager {
    private EditPart focusPart;
    private List<EditPart> highlighted;
    private EditPartViewer viewer;

    /**
     * Default Constructor
     * 
     * @since 3.2
     */
    protected HighlightManager() {
    	super();
    }

    /**
     * Creates the default implementation for a highlight manager.
     * 
     * @return the default selection manager
     * @since 3.2
     */
    public static HighlightManager createDefault() {
        return new HighlightManager();
    }

    /**
     * Appends the <code>EditPart</code> to the current highlighted elements.
     * Fires the highlighted changed to all.
     * 
     * @param editpart the EditPart to append
     */
    public void appendHighlight(EditPart editpart) {
        if (viewer != null && editpart != getFocus())
            viewer.setFocus(null);

        // if the editpart is already in the list, re-order it to be the last
        // one
        highlighted.remove(editpart);
        highlighted.add(editpart);

        if (editpart instanceof HighlightEditPart) {
            HighlightEditPart highlightable = (HighlightEditPart) editpart;
            highlightable.setHighlighted(true);
        }

        fireHighlightedChanged();
    }

    /**
     * Removes the <code>EditPart</code> from the current highlight.
     * 
     * @param editpart the editpart
     * @since 3.2
     */
    public void unHighlight(EditPart editpart) {

        if (editpart instanceof HighlightEditPart) {
            HighlightEditPart highlightable = (HighlightEditPart) editpart;
            highlightable.setHighlighted(false);
        }

        highlighted.remove(editpart);

        fireHighlightedChanged();
    }

    /**
     * Un highlight everything.
     * 
     */
    public void unHighlightAll() {
        EditPart part;

        for (int i = 0; i < highlighted.size(); i++) {
            part = highlighted.get(i);
            if (part instanceof HighlightEditPart) {
                HighlightEditPart highlightable = (HighlightEditPart) part;
                highlightable.setHighlighted(false);
            }

        }
        highlighted.clear();
        fireHighlightedChanged();
    }

    /**
     * Causes the viewer to fire highlighted changed notification to all
     * listeners. This is not (yet) implemented because it is not needed (yet).
     * 
     */
    protected void fireHighlightedChanged() {
        // no changed event (yet)
    }

    /**
     * Returns the focus editpart.
     * 
     * @return the focus editpart
     */
    protected EditPart getFocus() {
        return focusPart;
    }

    /**
     * Returns <code>null</code> or the viewer whose highlight is managed.
     * 
     * @return <code>null</code> or the viewer
     */
    protected EditPartViewer getViewer() {
        return viewer;
    }

    /**
     * For internal use only. This API is subject to change.
     * 
     * @param control the control
     */
    public void internalHookControl(Control control) {
    }

    /**
     * Provides a hook for when the viewer has been set.
     * 
     * @param viewer the viewer.
     */
    protected void hookViewer(EditPartViewer viewer) {
    }

    /**
     * For internal use only.
     * 
     * @param viewer viewer
     * @param highlighted Selected editparts
     * @param notifier notifier
     * @since 3.2
     */
    public void internalInitialize(EditPartViewer viewer,
            List<EditPart> highlighted, Runnable notifier) {
        this.viewer = viewer;
        this.highlighted = highlighted;

        hookViewer(viewer);
    }

    /**
     * Sets the focus part.
     * 
     * @param part the focus part
     */
    public void setFocus(EditPart part) {
        if (focusPart == part)
            return;
        if (focusPart != null)
            focusPart.setFocus(false);
        focusPart = part;
        if (focusPart != null)
            focusPart.setFocus(true);
    }

    /**
     * For internal use only. This API is subject to change.
     * 
     */
    public void internalUninstall() {
    }

}
