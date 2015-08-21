/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.actions;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.console.ConsoleFactory;
import org.rascalmpl.eclipse.console.ConsoleFactory.IRascalConsole;
import org.rascalmpl.eclipse.util.ResourcesToModules;

public class RunTests extends AbstractEditorAction {
	
	public RunTests(UniversalEditor editor) {
		super(editor, "Run Tests");
	}

	@Override
	public void run() {
		String mod = ResourcesToModules.moduleFromFile(file);
		IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager();
		
		for (IConsole console : man.getConsoles()) {
			if (console.getType().equals(ConsoleFactory.INTERACTIVE_CONSOLE_ID)) {
				IRascalConsole rascal = (IRascalConsole) console;

				try {
					rascal.activate();
					rascal.executeCommand("import " + mod + ";");
					rascal.executeCommand(":test");
					return;
				} catch (Throwable e) {
					Activator.getInstance().logException("run tests", e);
				}
			}
		}

		Activator.log("no console to run tests in was started yet", new NullPointerException());
	}
}
