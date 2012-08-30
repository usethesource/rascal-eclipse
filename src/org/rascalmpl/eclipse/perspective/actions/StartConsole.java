	package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.rascalmpl.eclipse.console.ConsoleFactory;

public class StartConsole implements IWorkbenchWindowActionDelegate, IObjectActionDelegate, IViewActionDelegate {
	private IProject selectedProject;

	@Override
	public void run(IAction action) {
		if (selectedProject == null) {
			ConsoleFactory.getInstance().openRunConsole();
		}
		else {
			ConsoleFactory.getInstance().launchConsole(selectedProject, ILaunchManager.DEBUG_MODE);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof StructuredSelection) {
			StructuredSelection ssel = (StructuredSelection) selection;
			
			Object elem = ssel.getFirstElement();
			if (elem instanceof IResource) {
				IResource resource = (IResource) elem;
				selectedProject = resource.getProject();
			}
			else if (elem instanceof IProject) {
				selectedProject = (IProject) elem;
			}
			else if (elem instanceof IJavaProject) {
				selectedProject = ((IJavaProject) elem).getProject();
			}
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public void init(IWorkbenchWindow window) {
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	@Override
	public void init(IViewPart view) {
	}
}
