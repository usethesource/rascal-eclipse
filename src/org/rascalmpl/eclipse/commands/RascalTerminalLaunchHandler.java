package org.rascalmpl.eclipse.commands;

import static org.rascalmpl.eclipse.repl.RascalTerminalRegistry.launchTerminal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class RascalTerminalLaunchHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the current selection
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		String project = null;
		
		if (selection != null && selection instanceof StructuredSelection) {
		    StructuredSelection s = (StructuredSelection) selection;
		    Object r = s.getFirstElement();

		    if (r instanceof IResource) {
		        project = ((IResource) r).getProject().getName();
		    }
		    else if (r instanceof IJavaProject) {
		        project = ((IJavaProject) r).getResource().getProject().getName();
		    }
		    
		    launchTerminal(project, "debug");
		}

		return null;
	}

    
}