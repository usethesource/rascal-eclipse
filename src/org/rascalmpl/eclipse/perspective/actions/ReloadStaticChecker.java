/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.perspective.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.IProgressService;
import org.rascalmpl.eclipse.Activator;

public class ReloadStaticChecker extends AbstractEditorAction {
	private final StaticCheckerHelper helper = new StaticCheckerHelper();
	
	public ReloadStaticChecker(UniversalEditor editor) {
		super(editor, "Reload static checker");
	}

	@Override
	public void run() {
		WorkspaceModifyOperation wmo = new WorkspaceModifyOperation(ResourcesPlugin.getWorkspace().getRoot()) {
			public void execute(IProgressMonitor monitor) {
				helper.reloadChecker(editor.getParseController().getProject());
			}
		};
		IProgressService ips = PlatformUI.getWorkbench().getProgressService();
		try {
			ips.run(true, true, wmo);
		} catch (InvocationTargetException e) {
			Activator.getInstance().logException("??", e);
		} catch (InterruptedException e) {
			Activator.getInstance().logException("??", e);
		}
	}
}
