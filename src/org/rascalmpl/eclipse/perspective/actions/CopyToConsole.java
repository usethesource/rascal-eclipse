/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.ConsoleFactory;
import org.rascalmpl.eclipse.console.ConsoleFactory.IRascalConsole;

public class CopyToConsole extends AbstractEditorAction {
	
	public CopyToConsole(UniversalEditor editor) {
		super(editor, "Copy to console");
		setImageDescriptor(Activator.getInstance().getImageRegistry().getDescriptor(IRascalResources.COPY_TO_CONSOLE));
	}

	@Override
	public void run() {
		String cmd = editor.getSelectionText();
		cmd = cmd.replaceAll("\n\n","\n");
		IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager();
		
		for (IConsole console : man.getConsoles()) {
			if (console.getType().equals(ConsoleFactory.INTERACTIVE_CONSOLE_ID)) {
				IRascalConsole rascal = (IRascalConsole) console;

				try {
					rascal.activate();
					rascal.executeCommand(cmd);
				} catch (Throwable e) {
					Activator.getInstance().logException("copyToConsole", e);
				}
			}
		}
	}
}
