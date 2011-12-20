/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.ConsoleFactory;
import org.rascalmpl.eclipse.console.ConsoleFactory.IRascalConsole;

public class CopyToConsole extends Action implements IEditorActionDelegate, IHandler {
	private UniversalEditor editor;

	public CopyToConsole() { }
	
	public CopyToConsole(UniversalEditor editor) {
		this.editor = editor;
		this.setText("Copy to console");
		setImageDescriptor(Activator.getInstance().getImageRegistry().getDescriptor(IRascalResources.COPY_TO_CONSOLE));
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof UniversalEditor) {
			this.editor = (UniversalEditor) targetEditor;
		}
		else {
			this.editor = null;
		}
	}
	
	public void run(IAction action) {
		run();
	}
	
	@Override
	public void run() {
		if (editor == null) {
			return;
		}
		
		String cmd = editor.getSelectionText();
		cmd = cmd.replaceAll("\n\n","\n");
		IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager();
		
		
		for (IConsole console : man.getConsoles()) {
			if (console.getType().equals(ConsoleFactory.INTERACTIVE_CONSOLE_ID)) {
				IRascalConsole rascal = (IRascalConsole) console;

				try {
					rascal.activate();
					rascal.executeCommand(cmd);
				} catch (Throwable e) {
					Activator.getInstance().logException("copyToConsole", e);
				}
			}
		}
		
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
		editor = null;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		run();
		return null;
	}

	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {
	}
}
