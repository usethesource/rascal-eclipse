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
import org.rascalmpl.eclipse.actions.BrowseTree;
import org.rascalmpl.eclipse.actions.CopyToConsole;
import org.rascalmpl.eclipse.actions.ImportInConsole;
import org.rascalmpl.eclipse.actions.ListAmbiguities;
import org.rascalmpl.eclipse.actions.ReloadStaticChecker;
import org.rascalmpl.eclipse.actions.ResetProjectState;
import org.rascalmpl.eclipse.actions.RunAmbiDexter;
import org.rascalmpl.eclipse.actions.RunStaticChecker;
import org.rascalmpl.eclipse.actions.RunTests;
import org.rascalmpl.eclipse.actions.StartConsole;
import org.rascalmpl.eclipse.actions.SubMenu;
import org.rascalmpl.eclipse.actions.TextTree;
import org.rascalmpl.eclipse.editor.highlight.ShowAsHTML;
import org.rascalmpl.eclipse.editor.highlight.ShowAsLatex;

public class ActionsContributor implements ILanguageActionsContributor {

	@Override
	public void contributeToEditorMenu(UniversalEditor editor,
			IMenuManager menuManager) {
		menuManager.add(new StartConsole(editor));
		menuManager.add(new CopyToConsole(editor));
		menuManager.add(new ImportInConsole(editor));
		 
		MenuManager exp = new SubMenu(menuManager, "Experimental");
		exp.add(new RunTests(editor));
		exp.add(new RunAmbiDexter(editor));
		exp.add(new ListAmbiguities(editor));
		exp.add(new RunStaticChecker(editor));
		exp.add(new ReloadStaticChecker(editor));
		exp.add(new ShowAsHTML(editor));
		exp.add(new ShowAsLatex(editor));
		
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
