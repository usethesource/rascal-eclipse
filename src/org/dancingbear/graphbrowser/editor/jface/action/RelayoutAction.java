/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.jface.action;

import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.eclipse.draw2d.Animation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Re-layout graph
 * 
 * @author Jeroen van Schagen
 * @date 03-03-2009
 */
public class RelayoutAction extends Action {

    private static final int ANIMATION_TIME = 500;
    private IWorkbenchPage page;

    public RelayoutAction(IWorkbenchPage page) {
        this.page = page;
    }

    /**
     * Execute relayout
     */
    @Override
    public void run() {
        if (page.getActiveEditor() instanceof GraphEditor) {
            Animation.markBegin();
            GraphEditor editor = (GraphEditor) page.getActiveEditor();
            editor.getController().applyLayout();
            Animation.run(ANIMATION_TIME);
            editor.getViewer().flush();
        }
    }

    /**
     * Get tooltip text of action
     * 
     * @return tooltipText
     */
    @Override
    public String getToolTipText() {
        return "Relayout graph";
    }

    /**
     * Get text of action
     * 
     * @return text
     */
    @Override
    public String getText() {
        return "Relayout";
    }

    /**
     * Get imagedescriptor of action (icon)
     * 
     * @return imageDescriptor
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

}