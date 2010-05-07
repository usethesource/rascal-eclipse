package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.EditorPluginAction;
import org.eclipse.ui.part.FileEditorInput;
import org.rascalmpl.eclipse.console.ConsoleFactory;

public class LaunchConsoleAction implements IObjectActionDelegate, IActionDelegate2, IEditorActionDelegate {
	IProject project;
	IFile file;

	public void dispose() {
		project = null;
		file = null;
	}

	public void init(IAction action) {}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void run(IAction action) {
		if (file != null) {
			ConsoleFactory.getInstance().openRunConsole(project, file);
		}
		else {
			ConsoleFactory.getInstance().openRunConsole(project);
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
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor.getEditorInput() instanceof FileEditorInput) {
			project = ((FileEditorInput) targetEditor.getEditorInput()).getFile().getProject();
		}
	}
}
