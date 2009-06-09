package org.meta_environment.rascal.eclipse.debug.ui.adapters;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalDebugTarget;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalStackFrame;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalThread;
import org.meta_environment.rascal.interpreter.DebuggableEvaluator;


public class StepModeAdapter  implements IObjectActionDelegate, IActionDelegate2 {

	private DebuggableEvaluator evaluator;
	
	public void dispose() {
		evaluator = null;
	}

	public void init(IAction action) {
		// TODO Auto-generated method stub
		
	}

	public void runWithEvent(IAction action, Event event) {
		// TODO Auto-generated method stub	
	}

	public void run(IAction action) {
		evaluator.setExpressionStepMode(true);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof RascalDebugTarget) {
				evaluator = ((RascalDebugTarget) element).getEvaluator();
				action.setEnabled(evaluator.expressionStepModeEnabled());
				return;
			}
			
		}
		action.setEnabled(false);
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
	}
	
}

