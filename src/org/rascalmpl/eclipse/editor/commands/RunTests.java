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
package org.rascalmpl.eclipse.editor.commands;

import java.util.Arrays;

import org.rascalmpl.eclipse.repl.RascalTerminalRegistry;
import org.rascalmpl.eclipse.util.ResourcesToModules;

import io.usethesource.impulse.editor.UniversalEditor;

public class RunTests extends AbstractEditorAction {
	
	public RunTests(UniversalEditor editor) {
		super(editor, "Run Tests");
	}

    @Override
	public void run() {
	    String mod = ResourcesToModules.moduleFromFile(file);
	    RascalTerminalRegistry.getInstance().queueCommands(project.getName(), Arrays.asList( 
	        "import " + mod + ";", 
	        ":test"
	    ));
	}
}
