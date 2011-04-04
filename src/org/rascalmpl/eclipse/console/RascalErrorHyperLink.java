/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - emilie.balland@inria.fr (INRIA)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
/**
 * 
 */
package org.rascalmpl.eclipse.console;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.part.FileEditorInput;
import org.rascalmpl.eclipse.Activator;

public class RascalErrorHyperLink implements IHyperlink {
	private final IFile file;
	private final int line;
	private final int col;

	RascalErrorHyperLink(IFile file, int line, int col) {
		this.line = line;
		this.col = col;
		this.file = file;
	}

	public void linkActivated() {
		openEditor(file, line, col);
	}

	public void linkEntered() {
	}

	public void linkExited() {
	}

	private void openEditor(IFile file, int line, int col) {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

		if (win != null) {
			IWorkbenchPage page = win.getActivePage();

			if (page != null) {
				try {
					page.openEditor(new FileEditorInput(file), UniversalEditor.EDITOR_ID);
				} catch (PartInitException e) {
					Activator.getInstance().logException("Could not open editor for: " + file.getName(), e);
				}
			}
		}
	}
}