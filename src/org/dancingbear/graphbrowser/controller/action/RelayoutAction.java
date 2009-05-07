/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.controller.action;

import org.dancingbear.graphbrowser.controller.EditorController;
import org.eclipse.draw2d.Animation;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * This is an example of a contextmenu action that is inserted by the
 * controller. The org.dancingbear.graphbrowser.editor.jface.action has a
 * duplicate of this class. This is for the crtl+r event handling. Pointing this
 * action (in TopologicalKeyHandler) to this class will create to great of a
 * dependency.
 * 
 * @author Menno Middel
 * @author Jeroen van Schagen
 * @date 13-03-09
 */
public class RelayoutAction extends Action implements IAction {

    private static final int ANIMATION_TIME = 500;
    private final EditorController controller;

    public RelayoutAction(EditorController controller) {
        this.controller = controller;
    }

    /**
     * Run relayout action
     */
    @Override
    public void run() {
        Animation.markBegin();
        controller.applyLayout();
        Animation.run(ANIMATION_TIME);
        GraphicalViewer viewer = controller.getEditor().getViewer();
        if (viewer != null) {
            viewer.flush();
        }
    }

    /**
     * Get Tooltip Text of this action
     * 
     * @return text Tooltip Text
     */
    @Override
    public String getToolTipText() {
        return "Relayout graph";
    }

    /**
     * Get text of this action
     * 
     * @return text Text of action
     */
    @Override
    public String getText() {
        return "Relayout (ctrl + r)";
    }

    /**
     * Get imagedescriptor of this action (icon)
     * 
     * @return imageDescriptor Imagedescriptor of this action
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }
}