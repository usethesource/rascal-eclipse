/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.ui.delegates;

import org.dancingbear.graphbrowser.editor.jface.action.ExcludeFilterAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Facade for ExcludeFilterAction
 * 
 * @author Erik Slagter
 * @author Jeroen van Schagen
 * 
 */
public class ExcludeFilterActionDelegate extends AbstractObjectActionDelegate {

    @Override
    public IAction createAction() {
        IWorkbenchPage page = getActivePart().getSite().getPage();
        ExcludeFilterAction action = new ExcludeFilterAction(page);
        return action;
    }

}