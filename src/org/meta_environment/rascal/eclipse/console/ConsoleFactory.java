package org.meta_environment.rascal.eclipse.console;

import org.eclipse.dltk.console.ScriptConsolePrompt;
import org.eclipse.dltk.console.ui.ScriptConsole;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;

public class ConsoleFactory implements IConsoleFactory {
	private RascalConsole fConsole;
	private IConsoleManager fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
	
	public void openConsole() {
		if (fConsole == null) {
			fConsole = new RascalConsole(); 

			fConsoleManager.addConsoles(new IConsole[]{fConsole});
		} 
		fConsoleManager.showConsoleView(fConsole);
	}
	
	protected class RascalConsole extends ScriptConsole 
	{
		public RascalConsole() {
			super("Rascal", "org.meta_environment.rascal.eclipse.console");
			setInterpreter(new RascalScriptInterpreter(this));
			setPrompt(new ScriptConsolePrompt(">", "?"));
		}
	}
}



