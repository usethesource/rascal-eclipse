/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef;

import java.util.ArrayList;
import java.util.List;

import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Contribute action bar actions to graph editor.
 * 
 * @author Jeroen van Lieshout
 * 
 */
public class GraphContextMenuContributor extends ContextMenuProvider {

    private ActionRegistry actionRegistry;
    protected ArrayList<IAction> actionList = new ArrayList<IAction>();

    public GraphContextMenuContributor(GraphEditor editor,
            ActionRegistry registry) {
        super(editor.getViewer());
        setActionRegistry(registry);
    }

    /**
     * This method builds up the contextmenu on a lazy way
     * 
     * @param menu The menumanager
     */
    @Override
    public void buildContextMenu(IMenuManager menu) {
        IAction action = null;

        GEFActionConstants.addStandardActionGroups(menu);
        action = getActionRegistry().getAction(ActionFactory.UNDO.getId());
        menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);
        action = getActionRegistry().getAction(ActionFactory.REDO.getId());
        menu.appendToGroup(GEFActionConstants.GROUP_UNDO, action);

        for (IAction currentAction : actionList) {
            menu.appendToGroup(GEFActionConstants.GROUP_REST, currentAction);
        }
    }

    /**
     * Get ActionRegistry
     * 
     * @return ActionRegisty
     */
    private ActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    /**
     * Set ActionRegistry
     * 
     * @param registry ActionRegisty to set
     */
    private void setActionRegistry(ActionRegistry registry) {
        actionRegistry = registry;
    }

    /**
     * Method to add action to contextmenu
     * 
     * @param action Action to add
     */
    public void addAction(IAction action) {
        actionList.add(action);
    }

    /**
     * Method to remove an action from the contextmenu
     * 
     * @param action Instance of action to remove
     */
    public void removeAction(IAction action) {
        actionList.remove(action);
    }

    /**
     * Get list of actions
     * 
     * @return list of actions
     */
    public List<IAction> getActionList() {
        return this.actionList;
    }

}