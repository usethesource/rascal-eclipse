package org.meta_environment.rascal.eclipse.console;

import org.eclipse.dltk.console.ScriptConsolePrompt;
import org.eclipse.dltk.console.ui.ScriptConsole;
import org.eclipse.dltk.console.ui.internal.ScriptConsoleViewer;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;

public class ConsoleFactory implements IConsoleFactory {
	public static final String CONSOLE_ID = "org.meta_environment.rascal.eclipse.console";

	private IConsoleManager fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
	
	public synchronized void openConsole() {
		RascalConsole console = new RascalConsole();
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
	}
	
	public class RascalConsole extends ScriptConsole{
		
		public RascalConsole(){
			super("Rascal", CONSOLE_ID);
			RascalScriptInterpreter interpreter = new RascalScriptInterpreter(this);
			interpreter.initialize();
			setInterpreter(interpreter);
			setPrompt(new ScriptConsolePrompt(">", "?"));
			addPatternMatchListener(new JumpToSource());
		}
		
		public ScriptConsoleViewer getViewer(){
			return (ScriptConsoleViewer) page.getViewer();
		}
	}
}



