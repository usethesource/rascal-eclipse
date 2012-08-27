/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.imp.editor.UniversalEditor;
import org.rascalmpl.eclipse.console.ConsoleFactory;

public class LaunchConsoleAction extends AbstractEditorAction {

	public LaunchConsoleAction(UniversalEditor editor) {
		super(editor, "Launch Console");
	}
	
	@Override
	public void run() {
		/*
		 * TODO: The console implementation and other parts of the
		 * Rascal Eclipse UI are implemented to depend on project specific
		 * information. This has to be resolved in order to safely allow launching
		 * consoles for files outside projects (e.g. a Rascal file opened with
		 * "File -> Open File ...").
		 */
		if (project != null) {
			ConsoleFactory.getInstance().openRunConsole(project);
		} else {
			ConsoleFactory.getInstance().openRunConsole();			
		}
	}
}
