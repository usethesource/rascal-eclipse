/*******************************************************************************
 * Copyright (c) 2009-2015 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.launch;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.rascalmpl.eclipse.repl.RascalTerminalRegistry;
import org.rascalmpl.eclipse.util.ResourcesToModules;

public class LaunchShortcut implements ILaunchShortcut {

    @Override
    public void launch(ISelection selection, String mode) {
        IFile file = (IFile) ((IStructuredSelection)selection).getFirstElement();
        launchWithResourceInformation(mode, file);
    }

    @Override
    public void launch(IEditorPart editor, String mode) {
		IResource resource = (IResource) editor.getEditorInput().getAdapter(IResource.class);
		launchWithResourceInformation(mode, resource);
    }
    
    private void launchWithResourceInformation(String mode, IResource resource) {
        String module = ResourcesToModules.moduleFromFile((IFile) resource);
        RascalTerminalRegistry.launchTerminal(resource.getProject().getName(), module, mode);
    }

}
