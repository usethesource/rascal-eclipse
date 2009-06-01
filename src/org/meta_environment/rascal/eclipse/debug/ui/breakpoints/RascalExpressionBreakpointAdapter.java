package org.meta_environment.rascal.eclipse.debug.ui.breakpoints;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.meta_environment.rascal.eclipse.debug.core.breakpoints.RascalExpressionBreakpoint;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalDebugTarget;

public class RascalExpressionBreakpointAdapter extends AbstractHandler implements IEditorActionDelegate {

	private ITextSelection fSelection;
	private IEditorPart editor;


	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editor = targetEditor;
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (fSelection == null) {
			return;
		}
		if (editor != null) {
			IResource resource = (IResource) editor.getEditorInput().getAdapter(IResource.class);
			//create the breakpoint and add it to the breakpoint manager
			try {
			RascalExpressionBreakpoint breakpoint = new RascalExpressionBreakpoint(resource, fSelection);
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(breakpoint);
			} catch (CoreException e) {
				// TODO: handle exception
			}
		}
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		fSelection = null;
		if (selection instanceof ITextSelection) {
			fSelection = (ITextSelection) selection;
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}


}
