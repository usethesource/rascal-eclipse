/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.editor;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.services.ILanguageActionsContributor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.rascalmpl.eclipse.perspective.actions.StartTutorAction;
import org.rascalmpl.eclipse.perspective.actions.SubMenu;
import org.rascalmpl.eclipse.perspective.actions.BrowseTree;
import org.rascalmpl.eclipse.perspective.actions.CopyToConsole;
import org.rascalmpl.eclipse.perspective.actions.ListAmbiguities;
import org.rascalmpl.eclipse.perspective.actions.ReloadStaticChecker;
import org.rascalmpl.eclipse.perspective.actions.ResetProjectState;
import org.rascalmpl.eclipse.perspective.actions.RunAmbiDexter;
import org.rascalmpl.eclipse.perspective.actions.RunStaticChecker;
import org.rascalmpl.eclipse.perspective.actions.TextTree;

public class ActionsContributor implements ILanguageActionsContributor {

	@Override
	public void contributeToEditorMenu(UniversalEditor editor,
			IMenuManager menuManager) {
		menuManager.add(StartTutorAction.getInstance().getAction());
		menuManager.add(new CopyToConsole(editor));
		
		MenuManager exp = new SubMenu(menuManager, "Experimental");
		exp.add(new RunAmbiDexter(editor));
		exp.add(new ListAmbiguities(editor));
		exp.add(new RunStaticChecker(editor));
		exp.add(new ReloadStaticChecker(editor));
		
		MenuManager devel = new SubMenu(menuManager, "Developers");
		devel.add(new ResetProjectState(editor));
		devel.add(new TextTree(editor));
		devel.add(new BrowseTree(editor));
	}

	@Override
	public void contributeToToolBar(final UniversalEditor editor,
			IToolBarManager toolbarManager) {
		
	}

	@Override
	public void contributeToStatusLine(UniversalEditor editor,
			IStatusLineManager statusLineManager) {
	}

	@Override
	public void contributeToMenuBar(UniversalEditor editor, IMenuManager menu) {
	}

}
