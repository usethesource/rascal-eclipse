package org.rascalmpl.eclipse.editor;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.services.ILanguageActionsContributor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.rascalmpl.checker.StaticChecker;

public class ActionContributor implements ILanguageActionsContributor {

	public void contributeToEditorMenu(UniversalEditor editor, IMenuManager menuManager) {
		menuManager.add(new Action("Reload Checker") {
			@Override
			public void run() {
				StaticChecker.getInstance().reload();
			}
		});
	}

	public void contributeToMenuBar(final UniversalEditor editor, IMenuManager menu) {
	}

	public void contributeToStatusLine(UniversalEditor editor, IStatusLineManager statusLineManager) {
	}

	public void contributeToToolBar(UniversalEditor editor, IToolBarManager toolbarManager) {
	}

	protected void copyToConsole(UniversalEditor editor) {
	}
}
