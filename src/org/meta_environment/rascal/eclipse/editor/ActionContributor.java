package org.meta_environment.rascal.eclipse.editor;

import java.io.IOException;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.services.ILanguageActionsContributor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.meta_environment.rascal.eclipse.Activator;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory.RascalConsole;

public class ActionContributor implements ILanguageActionsContributor {

	@Override
	public void contributeToEditorMenu(UniversalEditor editor,
			IMenuManager menuManager) {
	}

	@Override
	public void contributeToMenuBar(UniversalEditor editor, IMenuManager menu) {
	}

	@Override
	public void contributeToStatusLine(UniversalEditor editor,
			IStatusLineManager statusLineManager) {
	}

	@Override
	public void contributeToToolBar(final UniversalEditor editor,
			IToolBarManager toolbarManager) {
		IAction action = new Action("Copy to Console") {
			@Override
			public void run() {
				copyToConsole(editor);
			}
			
			@Override
			public int getAccelerator() {
				return SWT.CONTROL + SWT.SHIFT + SWT.TAB;
			}
			
		};
		toolbarManager.add(action);
	}

	protected void copyToConsole(UniversalEditor editor) {
		String cmd = editor.getSelectionText();
		IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager();
		
		for (IConsole console : man.getConsoles()) {
			if (console.getType().equals(ConsoleFactory.CONSOLE_ID)) {
				ConsoleFactory.RascalConsole rascal = (RascalConsole) console;
				try {
					rascal.activate();
					String result = rascal.handleCommand(cmd);
					System.err.println(result);
				} catch (Throwable e) {
					Activator.getInstance().logException("copyToConsole", e);
				}
			}
		}
	}
}
