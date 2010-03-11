package org.rascalmpl.eclipse.editor;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.services.ILanguageActionsContributor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.rascalmpl.checker.StaticChecker;

public class ActionContributor implements ILanguageActionsContributor {

	private class EnableTypeCheckerMenuItem extends Action {

		public EnableTypeCheckerMenuItem(String text) {
			super(text, IAction.AS_CHECK_BOX);
			if (StaticChecker.getInstance().isPassEnabled(StaticChecker.TYPECHECKER))
				this.setChecked(true);
			else
				this.setChecked(false);
		}

		@Override
		public void run() {
			if (StaticChecker.getInstance().isPassEnabled(StaticChecker.TYPECHECKER)) {
				StaticChecker.getInstance().disablePipelinePass(StaticChecker.TYPECHECKER);
				this.setChecked(false);
			} else {
				StaticChecker.getInstance().enablePipelinePass(StaticChecker.TYPECHECKER);
				this.setChecked(true);
			}
		}
	}
	
	public void contributeToEditorMenu(UniversalEditor editor, IMenuManager menuManager) {
		menuManager.add(new Action("Load Checker") {
			@Override
			public void run() {
				StaticChecker.getInstance().reload();
			}
		});

		menuManager.add(new EnableTypeCheckerMenuItem("Enable Checker"));
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
