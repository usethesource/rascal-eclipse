/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
*******************************************************************************/
package org.rascalmpl.eclipse.perspective.actions;

import java.net.URI;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
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
import org.rascalmpl.eclipse.debug.core.model.RascalDebugElement;
import org.rascalmpl.eclipse.debug.core.model.RascalDebugTarget;
import org.rascalmpl.interpreter.result.Result;


public class EvaluateExpression extends AbstractHandler implements IEditorActionDelegate {

	private ITextSelection fSelection;

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (fSelection == null) {
			return;
		}
		String expr = fSelection.getText();
		if(expr != null && expr.trim().length() > 0){
			evaluate(expr);
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
			super(shell, point, "rascal.EvaluateExpression");
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

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object context = event.getApplicationContext();
		if (context instanceof EvaluationContext) {
			EvaluationContext evalContext = (EvaluationContext) context;
			Object selection = evalContext.getVariable("selection");
			if(selection instanceof ITextSelection){
				String expr = ((ITextSelection)selection).getText();
				if(expr != null && expr.trim().length() > 0){
					evaluate(expr);
				}
			}
		}
		return null;
	}

	private void evaluate(String expr) {

		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		//TODO: create a point exactly at the beginning of the selected text
		Point point = PlatformUI.getWorkbench().getDisplay().getCursorLocation();
		IAdaptable object = DebugUITools.getDebugContext();
		RascalDebugTarget target = null;
		if (object instanceof RascalDebugElement) {
			target = ((RascalDebugElement) object).getRascalDebugTarget();
		} else if (object instanceof ILaunch) {
			target = (RascalDebugTarget) ((ILaunch) object).getDebugTarget();
		}

		//evaluate
		Result<org.eclipse.imp.pdb.facts.IValue> value = target.getEvaluator().eval(null, expr, URI.create("debug:///"));

		try {
			String result = null;
			if (value != null) {
				result = expr+"\n"+value.toString();
				DisplayPopup popup = new DisplayPopup(shell, point, result);
				popup.open();
			}
		} catch (Throwable t) {
			DebugPlugin.log(t);
		}
	}

}
