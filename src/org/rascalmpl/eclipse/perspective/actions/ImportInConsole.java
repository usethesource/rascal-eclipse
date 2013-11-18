/*******************************************************************************
 * Copyright (c) 2009-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *   * Anya Helene Bagge - anya@ii.uib.no - UiB
 *******************************************************************************/
package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.console.ConsoleFactory;
import org.rascalmpl.eclipse.console.ConsoleFactory.IRascalConsole;
import org.rascalmpl.eclipse.util.RascalKeywords;
import org.rascalmpl.eclipse.util.ResourcesToModules;

public class ImportInConsole extends AbstractEditorAction implements IWorkbenchWindowActionDelegate, IObjectActionDelegate, IViewActionDelegate {
	public ImportInConsole() {
		super(null, "Import Current Module in Console");
	}
	
	public ImportInConsole(UniversalEditor editor) {
		super(editor, "Import Current Module in Console");
	}

	@Override
	public void run() {
		String mod = RascalKeywords.escapeName(ResourcesToModules.moduleFromFile(file));
		IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager();

		
		for (IConsole console : man.getConsoles()) {
			if (console.getType().equals(ConsoleFactory.INTERACTIVE_CONSOLE_ID)) {
				IRascalConsole rascal = (IRascalConsole) console;

				try {
					rascal.activate();
					rascal.executeCommand("import " + mod + ";");
				} catch (Throwable e) {
					Activator.getInstance().logException("importInConsole", e);
				}
			}
		}
	}

	@Override
	public void run(IAction action) {
		run();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof StructuredSelection) {
			StructuredSelection ssel = (StructuredSelection) selection;
			
			Object elem = ssel.getFirstElement();
			if (elem instanceof IFile) {
				IFile file = (IFile) elem;
				project = file.getProject();
				this.file = file;
			}
		}
	}

	@Override
	public void init(IViewPart view) {
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if (targetPart instanceof UniversalEditor) {
			UniversalEditor editor = (UniversalEditor) targetPart;
			IFile file = initFile(editor, editor.getParseController().getProject());
			
			if (file != null) {
				project = file.getProject();
				this.file = file;
			}
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
	}
}
