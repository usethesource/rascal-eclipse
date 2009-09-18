package org.meta_environment.rascal.eclipse.debug.ui.actions;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalVariable;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalVariableValue;
import org.meta_environment.rascal.eclipse.lib.View;

public class OpenGraphEditor implements IObjectActionDelegate, IActionDelegate2 {

	private RascalVariable var;

	public void dispose() {
		var = null;
	}

	public void init(IAction action) {}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void run(IAction action) {
		try {
			View.show(((RascalVariableValue)var.getValue()).getValue());
		} catch (DebugException e) {
			throw new RuntimeException(e);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof RascalVariable) {
				var = (RascalVariable) element;
				//test if the variable type is a relation
				if (var.isRelation()) {
					action.setEnabled(true);
				} else {
					action.setEnabled(false);
				}
			}
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

}
