/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Bert Lisser    - Bert.Lisser@cwi.nl
 *******************************************************************************/
package org.rascalmpl.eclipse.perspective.actions;

import java.util.List;

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
import org.rascalmpl.eclipse.console.ConsoleFactory.IRascalConsole;
import org.rascalmpl.eclipse.console.internal.CommandExecutionException;
import org.rascalmpl.eclipse.console.internal.TerminationException;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.env.Pair;
import org.rascalmpl.interpreter.result.AbstractFunction;
import org.rascalmpl.interpreter.result.OverloadedFunctionResult;

public class LaunchRunConsoleAction extends Action implements
		IObjectActionDelegate, IActionDelegate2, IEditorActionDelegate {
	IProject project;
	IFile file;

	public LaunchRunConsoleAction() {
	}

	public LaunchRunConsoleAction(IProject project, IFile file) {
		this.project = project;
		this.file = file;
		setImageDescriptor(Activator.getInstance().getImageRegistry()
				.getDescriptor(IRascalResources.RASCAL_DEFAULT_IMAGE));
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

	public void init(IAction action) {
	}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void run(IAction action) {
		run();
	}

	static public void process(IProject project, IFile file) {
		IRascalConsole console = ConsoleFactory.getInstance().openRunConsole(
				project);
		String moduleFullName = file.getName();
		moduleFullName = moduleFullName.substring(0, moduleFullName.length()
				- Configuration.RASCAL_FILE_EXT.length());
		try {
			console.getRascalInterpreter().execute(
					"import " + moduleFullName + ";");	
			List<Pair<String, OverloadedFunctionResult>> functions = 
					console.getRascalInterpreter().getEval().getCurrentEnvt().getImport(moduleFullName).getFunctions();
			for (Pair<String, OverloadedFunctionResult> f:functions) {
				if (f.getFirst().equals("main")) {
					for (AbstractFunction g: f.getSecond().iterable()) {
						if (g.getArity()==0) {
							console.getRascalInterpreter().execute("main();");
							return;					
						}
					}			
				}
			}	
			final String s = "Procedure \"main()\" not found in module "+moduleFullName;
			console.getRascalInterpreter().getEval().getStdErr().println(s);
			System.err.println(s);
		} catch (CommandExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TerminationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		process(project, file);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof IProject) {
				project = (IProject) element;
				file = null;
			} else if (element instanceof IFolder) {
				project = ((IFolder) element).getProject();
				file = null;
			} else if (element instanceof IFile) {
				project = ((IFile) element).getProject();
				file = (IFile) element;
			}
		}
		update();
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor != null
				&& targetEditor.getEditorInput() instanceof FileEditorInput) {
			project = ((FileEditorInput) targetEditor.getEditorInput())
					.getFile().getProject();
			file = ((FileEditorInput) targetEditor.getEditorInput()).getFile();
		}
	}
}
