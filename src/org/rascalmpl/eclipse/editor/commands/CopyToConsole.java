/*******************************************************************************
 * Copyright (c) 2009-2015 CWI
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

import io.usethesource.impulse.editor.UniversalEditor;
import org.rascalmpl.eclipse.repl.RascalTerminalRegistry;

public class CopyToConsole extends AbstractEditorAction {
	
	public CopyToConsole(UniversalEditor editor) {
		super(editor, "Execute Selected Text in Console");
	}

	@Override
	public void run() {
		String cmd = editor.getSelectionText();
		String projectName = editor.getParseController().getProject().getRawProject().getName();
        RascalTerminalRegistry.getInstance().queueCommand(projectName, cmd);
	}
}
