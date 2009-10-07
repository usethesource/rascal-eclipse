package org.meta_environment.rascal.eclipse.perspective.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.meta_environment.rascal.eclipse.Activator;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory.IRascalConsole;

public class CopyToConsole extends AbstractHandler implements IEditorActionDelegate {
	private UniversalEditor editor;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof UniversalEditor) {
			this.editor = (UniversalEditor) targetEditor;
		}
		else {
			this.editor = null;
		}
	}
	
	public void run(IAction action) {
		if (editor == null) {
			return;
		}
		
		String cmd = editor.getSelectionText();
		IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager();
		
		
		for (IConsole console : man.getConsoles()) {
			if (console.getType().equals(ConsoleFactory.INTERACTIVE_CONSOLE_ID)) {
				IRascalConsole rascal = (IRascalConsole) console;
				
				try {
					rascal.activate();
					rascal.executeCommand(cmd);
				} catch (Throwable e) {
					Activator.getInstance().logException("copyToConsole", e);
				}
			}
		}
		
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		run(null);
		return null;
	}
}
