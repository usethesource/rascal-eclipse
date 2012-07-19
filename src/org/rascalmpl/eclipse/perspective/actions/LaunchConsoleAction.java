/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.FileEditorInput;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.ConsoleFactory;

public class LaunchConsoleAction extends Action implements IObjectActionDelegate, IActionDelegate2, IEditorActionDelegate {
	IProject project;
	IFile file;

	public LaunchConsoleAction() { }
	
	public LaunchConsoleAction(IProject project, IFile file) {
		this.project = project;
		this.file = file;
		setImageDescriptor(Activator.getInstance().getImageRegistry().getDescriptor(IRascalResources.RASCAL_DEFAULT_IMAGE));
		update();
	}
	
	public void dispose() {
		project = null;
		file = null;
	}
	
	private void update() {
		if (project != null) {
			setEnabled(true);
			String msg = "Launch console for " + project.getName();
			setToolTipText(msg);
			setText(msg);
		}
	}

	public void init(IAction action) {}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void run(IAction action) {
		run();
	}
	
	@Override
	public void run() {
		/*
		 * NOTE: The console implementation and other parts of the
		 * Rascal Eclipse UI are implemented to depend on project specific
		 * information. This has to be resolved in order to safely allow launching
		 * consoles for files outside projects (e.g. a Rascal file opened with
		 * "File -> Open File ...").
		 */
		if (project != null) {
			ConsoleFactory.getInstance().openRunConsole(project);
		} else {
			ConsoleFactory.getInstance().openRunConsole();			
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof IProject) {
				project = (IProject) element;
				file = null;
			}
			else if (element instanceof IFolder) {
				project = ((IFolder) element).getProject();
				file = null;
			}
			else if (element instanceof IFile) {
				project = ((IFile) element).getProject();
				file = (IFile) element;
			}
		}
		update();
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null && targetEditor.getEditorInput() instanceof FileEditorInput) {
			project = ((FileEditorInput) targetEditor.getEditorInput()).getFile().getProject();
		}
	}
}
