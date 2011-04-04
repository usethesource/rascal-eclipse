/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.console.internal;

import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.TextConsoleViewer;

public class InterpreterConsoleViewer extends TextConsoleViewer{
	private final InteractiveInterpreterConsole console;
	private final CommandHistory history;
	
	private StyledText styledText;

	public InterpreterConsoleViewer(InteractiveInterpreterConsole console, Composite parent){
		super(parent, console);
		
		this.console = console;
		this.history = console.getHistory();
		
		styledText.removeLineStyleListener(this);
	}
	
	public StyledText createTextWidget(Composite parent, int styles){
		return (styledText != null) ? styledText : (styledText = new InterpreterConsoleStyledText(parent, styles));
	}

	private class InterpreterConsoleStyledText extends StyledText{

		public InterpreterConsoleStyledText(Composite parent, int style){
			super(parent, style);
		}

		public void invokeAction(int action){
			switch(action){
				case ST.LINE_UP:
					history.updateCurrent(console.getCurrentConsoleInput());
					String previousCommand = history.getPreviousCommand();
					console.historyCommand(previousCommand);
					return;
				case ST.LINE_DOWN:
					history.updateCurrent(console.getCurrentConsoleInput());
					String nextCommand = history.getNextCommand();
					console.historyCommand(nextCommand);
					return;
			}

			super.invokeAction(action);
		}
	}
}
