package org.meta_environment.rascal.eclipse.perspective.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory;

public class LaunchConsoleAction implements IObjectActionDelegate, IActionDelegate2 {
	
	IProject project;

	public void dispose() {
		project = null;
	}

	public void init(IAction action) {}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void run(IAction action) {
		ConsoleFactory.getInstance().openRunConsole(project);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof IProject) {
				project = (IProject) element;
			}
			else if (element instanceof IFolder) {
				project = ((IFolder) element).getProject();
			}
			else if (element instanceof IFile) {
				project = ((IFile) element).getProject();
			}
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

}
