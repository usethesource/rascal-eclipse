/*******************************************************************************
 * Copyright (c) 2009-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *   * Anya Helene Bagge - anya@ii.uib.no - UiB
 *******************************************************************************/
package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.console.ConsoleFactory;
import org.rascalmpl.eclipse.console.ConsoleFactory.IRascalConsole;
import org.rascalmpl.eclipse.util.RascalKeywords;
import org.rascalmpl.eclipse.util.ResourcesToModules;

public class ImportInConsole extends AbstractEditorAction {


	public ImportInConsole(UniversalEditor editor) {
		super(editor, "Import Current Module in Console");
	}

	@Override
	public void run() {
		String mod = RascalKeywords.escapeName(ResourcesToModules.moduleFromFile(file));
		IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager();

		
		for (IConsole console : man.getConsoles()) {
			if (console.getType().equals(ConsoleFactory.INTERACTIVE_CONSOLE_ID)) {
				IRascalConsole rascal = (IRascalConsole) console;

				try {
					rascal.activate();
					rascal.executeCommand("import " + mod + ";");
				} catch (Throwable e) {
					Activator.getInstance().logException("importInConsole", e);
				}
			}
		}
	}
}
