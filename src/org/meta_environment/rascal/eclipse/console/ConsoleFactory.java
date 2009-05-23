package org.meta_environment.rascal.eclipse.console;

import org.eclipse.dltk.console.ScriptConsolePrompt;
import org.eclipse.dltk.console.ui.ScriptConsole;
import org.eclipse.dltk.console.ui.internal.ScriptConsoleViewer;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalThread;
import org.meta_environment.rascal.interpreter.IDebugger;

public class ConsoleFactory implements IConsoleFactory {
	public static final String CONSOLE_ID = "org.meta_environment.rascal.eclipse.console";

	private static ConsoleFactory instance;

	private IConsoleManager fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();

	public ConsoleFactory() {}

	public static ConsoleFactory getInstance() {
		if (instance == null) {
			instance = new ConsoleFactory();
		} 
		return instance;
	}

	public void openConsole(){
		RascalConsole console = new RascalConsole();
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
	}

	public class RascalConsole extends ScriptConsole{

		protected RascalScriptInterpreter interpreter;

		public RascalConsole(){
			super("Rascal", CONSOLE_ID);
			interpreter = new RascalScriptInterpreter(this);
			interpreter.initialize();
			setInterpreter(interpreter);
			setPrompt(new ScriptConsolePrompt(">", "?"));
			addPatternMatchListener(new JumpToSource());
		}
		
		public RascalScriptInterpreter getInterpreter() {
			return interpreter;			
		}

		public ScriptConsoleViewer getViewer(){
			return (ScriptConsoleViewer) page.getViewer();
		}

		public void setDebugger(IDebugger debugger) {
			interpreter.setDebugger(debugger);
		}

	}


}



