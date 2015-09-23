	package org.rascalmpl.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.imp.editor.UniversalEditor;
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
import org.rascalmpl.eclipse.editor.commands.AbstractEditorAction;
import org.rascalmpl.eclipse.repl.RascalTerminalRegistry;

public class StartConsole extends AbstractEditorAction implements IWorkbenchWindowActionDelegate, IObjectActionDelegate, IViewActionDelegate {
	public StartConsole() {
		super(null, "Start Console");
	}
	
	public StartConsole(UniversalEditor editor) {
		super(editor, "Start Console");
	}

	@Override
	public void run(IAction action) {
		run();
	}
	
	@Override
	public void run() {
		if (project == null) {
		    // TODO: set up a terminal with a configuration window
		    RascalTerminalRegistry.launchTerminal(null, ILaunchManager.DEBUG_MODE);
		}
		else {
		    RascalTerminalRegistry.launchTerminal(project.getName(), ILaunchManager.DEBUG_MODE);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof StructuredSelection) {
			StructuredSelection ssel = (StructuredSelection) selection;
			
			Object elem = ssel.getFirstElement();
			if (elem instanceof IResource) {
				IResource resource = (IResource) elem;
				project = resource.getProject();
			}
			else if (elem instanceof IProject) {
				project = (IProject) elem;
			}
			else if (elem instanceof IJavaProject) {
				project = ((IJavaProject) elem).getProject();
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
	public void init(IViewPart view) {
	}
}
