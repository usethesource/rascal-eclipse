package org.meta_environment.rascal.eclipse.debug.ui.actions;

import java.io.IOException;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.DebugPopup;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalDebugElement;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalDebugTarget;
import org.meta_environment.rascal.interpreter.result.Result;


public class EvaluateExprAction implements IEditorActionDelegate {

	private ITextSelection fSelection;

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (fSelection == null) {
			return;
		}
		String text = fSelection.getText();
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		Point point = PlatformUI.getWorkbench().getDisplay().getCursorLocation();
		IAdaptable object = DebugUITools.getDebugContext();
		RascalDebugTarget target = null;
		if (object instanceof RascalDebugElement) {
			target = ((RascalDebugElement) object).getRascalDebugTarget();
		} else if (object instanceof ILaunch) {
			target = (RascalDebugTarget) ((ILaunch) object).getDebugTarget();
		}
		try {
			//parse the expression
			org.meta_environment.rascal.ast.Expression ast = target.getInterpreter().getExpression(text);

			//evaluate
			Result<org.eclipse.imp.pdb.facts.IValue> value = target.getEvaluator().eval(ast);

			try {
				String result = null;
				if (value != null) {
					result = text+"\n"+value.toString();
					DisplayPopup popup = new DisplayPopup(shell, point, result);
					popup.open();
				}
			} catch (Throwable t) {
				DebugPlugin.log(t);
			}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.eclipse.jdt.internal.debug.ui.actions.PopupDisplayAction.DisplayPopup
	 */
	private class DisplayPopup extends DebugPopup {
		public DisplayPopup(Shell shell, Point point, String text) {
			super(shell, point, "rascal.evaluateaction");
			this.text = text;
		}

		protected String getActionText() {
			return "Move to Display view";
		}

		private String text;

		protected Control createDialogArea(Composite parent) {
			GridData gd = new GridData(GridData.FILL_BOTH);
			StyledText text = new StyledText(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL);
			text.setLayoutData(gd);

			text.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

			text.setText(this.text);

			return text;
		}
	}


}
