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

import org.eclipse.imp.utils.Pair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.TextConsoleViewer;

public class InterpreterConsoleViewer extends TextConsoleViewer{
	private final InteractiveInterpreterConsole console;
	private final CommandHistory history;
	
	private InterpreterConsoleStyledText styledText;
	private CommandFragmentCompletion completion;
	
	public InterpreterConsoleViewer(InteractiveInterpreterConsole console, Composite parent){
		super(parent, console);
		
		this.console = console;
		this.history = console.getHistory();
		this.completion = console.getCompletion();
		styledText.removeLineStyleListener(this);
	}
	
	public StyledText createTextWidget(Composite parent, int styles){
		if (styledText == null) {
		  styledText = new InterpreterConsoleStyledText(parent, styles);
		}
		
		return styledText;
	}
	
	
	public void setEditable(boolean editable){
		super.setEditable(editable);
		
		InterpreterConsoleStyledText styledText = this.styledText;
		if(styledText != null){
			if(editable){
				styledText.enable();
			}else{
				styledText.disable();
			}
		}
	}

	private class InterpreterConsoleStyledText extends StyledText{
		private boolean enabled;
		private int completionOffset;
		private int completionPreviousLength;
		private final int COMPLETION_FORWARD = 18000001;
		//private final int COMPLETION_BACKWARD = 18000002;
		private final int HISTORY_FORWARD = 18000011;
		private final int HISTORY_BACKWARD = 18000012;

		public InterpreterConsoleStyledText(Composite parent, int style){
			super(parent, style);
			setKeyBinding(SWT.MOD1 + 'R', HISTORY_BACKWARD);
			setKeyBinding(SWT.MOD1 + SWT.MOD3 + 'R', HISTORY_FORWARD);
			setKeyBinding(SWT.TAB, COMPLETION_FORWARD);
			//setKeyBinding(SWT.TAB + SWT.SHIFT, COMPLETION_BACKWARD); // not yet possible with the possible framework
			enable();
		}
		
		@Override
		public synchronized void invokeAction(int action){
			if(!enabled) return;
			
			switch(action){
				case COMPLETION_FORWARD:
					if (!completion.isCompleting()) {
						Pair<Integer, Integer> offsetLength = completion.start(console.getCurrentCursorPosition(), console.getCurrentConsoleInput());
						this.completionOffset = offsetLength.first ;
						this.completionPreviousLength = offsetLength.second ;
					}
					if (completion.isCompleting()) {
						String next = completion.nextCompletion();
						console.replaceCompletion(completionOffset, completionPreviousLength, next);
						completionPreviousLength = next.length();
					}
					return;
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
				case ST.LINE_START:
					setCaretOffset(console.getInputOffset());
					return;
				case HISTORY_FORWARD:
				case HISTORY_BACKWARD:
				  if (history.isSearching()) {
				    if (action == HISTORY_BACKWARD) {
				      console.historyCommand(history.findNextCommand());
				    }
				    else {
				      console.historyCommand(history.findPreviousCommand());
				    }
				  }
				  else {
				    console.historyCommand(history.findCommand(console.getCurrentConsoleInput()));
				  }
				  return;
			}

			super.invokeAction(action);
			
			if(action == ST.COLUMN_PREVIOUS){
				int inputOffset = console.getInputOffset();
				int caretOffset = getCaretOffset();
				if(caretOffset < inputOffset){
					setCaretOffset(inputOffset);
				}
			}
		}
		
		public synchronized void enable(){
			enabled = true;
		}
		
		public synchronized void disable(){
			enabled = false;
		}
	}
}
