package org.meta_environment.rascal.eclipse.debug.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalDebugTarget;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalStackFrame;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalThread;
import org.meta_environment.rascal.interpreter.DebuggableEvaluator;


public class ExpressionStepModeAction  implements IObjectActionDelegate, IActionDelegate2 {

	private DebuggableEvaluator evaluator;

	public void dispose() {
		evaluator = null;
	}

	public void init(IAction action) {}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void run(IAction action) {
		evaluator.setExpressionStepMode(! evaluator.expressionStepModeEnabled());
		action.setChecked(evaluator.expressionStepModeEnabled());
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof RascalDebugTarget) {
				evaluator = ((RascalDebugTarget) element).getEvaluator();
			}
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {}

}

