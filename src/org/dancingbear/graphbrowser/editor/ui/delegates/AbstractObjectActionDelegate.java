/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.ui.delegates;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * IObjectActionDelegate facade, which implements the default accessor and
 * modifier functionality.
 * 
 * see IObjectActionDelegate
 * 
 * @author Jeroen van Schagen
 * @date 10-03-2009
 */
public abstract class AbstractObjectActionDelegate implements
        IObjectActionDelegate {

    private StructuredSelection selection;
    private IWorkbenchPart part;

    /**
     * see {@link IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)}
     */
    public void setActivePart(IAction action, IWorkbenchPart part) {
        this.part = part;
    }

    /**
     * see {@link IObjectActionDelegate#selectionChanged(IAction, ISelection)}
     */
    public final void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof StructuredSelection) {
            this.selection = (StructuredSelection) selection;
        }
    }

    /**
     * Create action to be executed.
     * 
     * @return action
     */
    public abstract IAction createAction();

    /**
     * Execute created action.
     */
    public final void run(IAction action) {
        try {
            IAction relatedAction = createAction();
            relatedAction.run();
        } catch (NullPointerException e) {
            // TODO: Process error
        }
    }

    /**
     * Retrieve selection.
     * 
     * @return Selection
     */
    public final StructuredSelection getSelection() {
        return selection;
    }

    /**
     * Change active part
     * 
     * @return Active workbench part
     */
    public final IWorkbenchPart getActivePart() {
        return part;
    }

}