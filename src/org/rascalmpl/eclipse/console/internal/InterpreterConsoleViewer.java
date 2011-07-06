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

import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.rascalmpl.eclipse.IRascalResources;

public class InterpreterConsoleViewer extends TextConsoleViewer{
	private final InteractiveInterpreterConsole console;
	private final CommandHistory history;
	
	private InterpreterConsoleStyledText styledText;

	private class PromptManager implements CaretListener {
		private InterpreterConsoleStyledText txt;

		public PromptManager(InterpreterConsoleStyledText txt) {
			this.txt = txt;
		}
		
		@Override
		public void caretMoved(CaretEvent event) {
			int line = txt.getLineAtOffset(event.caretOffset);
			Point loc = txt.getLocationAtOffset(event.caretOffset);
			
			String lineText = txt.getLine(line);
			
			if (lineText.length() > 0) {
				int endOfPromptOffset = txt.getOffsetAtLine(line) + IRascalResources.RASCAL_PROMPT.length();
				Point lineLoc = txt.getLocationAtOffset(endOfPromptOffset);

				if (lineText.startsWith(IRascalResources.RASCAL_PROMPT) && loc.x < lineLoc.x) {
					txt.setCaretOffset(txt.getOffsetAtLine(line) + IRascalResources.RASCAL_PROMPT.length());
				}
			}
		}
	}
	
	public InterpreterConsoleViewer(InteractiveInterpreterConsole console, Composite parent){
		super(parent, console);
		
		this.console = console;
		this.history = console.getHistory();
		
		styledText.removeLineStyleListener(this);
		styledText.addCaretListener(new PromptManager(styledText));
	}
	
	public StyledText createTextWidget(Composite parent, int styles){
		return (styledText != null) ? styledText : (styledText = new InterpreterConsoleStyledText(parent, styles));
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

		public InterpreterConsoleStyledText(Composite parent, int style){
			super(parent, style);
			
			enable();
		}

		public synchronized void invokeAction(int action){
			if(!enabled) return;
			
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
		
		public synchronized void enable(){
			enabled = true;
		}
		
		public synchronized void disable(){
			enabled = false;
		}
	}
}
