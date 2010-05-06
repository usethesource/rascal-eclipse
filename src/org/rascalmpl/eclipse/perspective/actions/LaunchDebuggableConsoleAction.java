package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
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
import org.rascalmpl.eclipse.IRascalResources;

public class LaunchDebuggableConsoleAction implements IObjectActionDelegate, IActionDelegate2, IEditorActionDelegate {

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

		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(IRascalResources.ID_RASCAL_LAUNCH_CONFIGURATION_TYPE);

		// create a new configuration which will launch the debuggable console
		ILaunchConfigurationWorkingCopy workingCopy;
		try {
			workingCopy = type.newInstance(null, "console debug");
			//set the project attribute
			workingCopy.setAttribute(IRascalResources.ATTR_RASCAL_PROJECT, project.getFullPath().toString());
			ILaunchConfiguration configuration = workingCopy.doSave();
				DebugUITools.launch(configuration, ILaunchManager.DEBUG_MODE);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor.getEditorInput() instanceof FileEditorInput) {
			project = ((FileEditorInput) targetEditor.getEditorInput()).getFile().getProject();
		}
	}

}