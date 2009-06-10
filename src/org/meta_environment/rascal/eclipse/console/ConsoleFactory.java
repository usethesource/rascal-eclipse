package org.meta_environment.rascal.eclipse.console;

import java.io.PrintWriter;

import org.eclipse.dltk.console.ScriptConsolePrompt;
import org.eclipse.dltk.console.ui.ScriptConsole;
import org.eclipse.dltk.console.ui.internal.ScriptConsoleViewer;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.meta_environment.ValueFactoryFactory;
import org.meta_environment.rascal.interpreter.DebuggableEvaluator;
import org.meta_environment.rascal.interpreter.Evaluator;
import org.meta_environment.rascal.interpreter.IDebugger;
import org.meta_environment.rascal.interpreter.env.ModuleEnvironment;

public class ConsoleFactory implements IConsoleFactory {
	public static final String CONSOLE_ID = "org.meta_environment.rascal.eclipse.console";
	private final static IValueFactory vf = ValueFactoryFactory.getValueFactory();


	private static ConsoleFactory instance;
	
	private RascalConsole lastConsole;

	private IConsoleManager fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();

	public ConsoleFactory() {
		super();
	}

	public static ConsoleFactory getInstance() {
		if (instance == null) {
			instance = new ConsoleFactory();
		} 
		return instance;
	}

	public void openConsole(){
		lastConsole = new RascalConsole();
		fConsoleManager.addConsoles(new IConsole[]{lastConsole});
		fConsoleManager.showConsoleView(lastConsole);
	}
	
	public void openDebuggableConsole(IDebugger debugger){
		lastConsole = new RascalConsole(debugger);
		fConsoleManager.addConsoles(new IConsole[]{lastConsole});
		fConsoleManager.showConsoleView(lastConsole);
	}
	
	public RascalConsole getLastConsole() {
		return lastConsole;
	}

	public class RascalConsole extends ScriptConsole{

		protected RascalScriptInterpreter interpreter;

		public RascalConsole(){
			super("Rascal", CONSOLE_ID);
			Evaluator eval = new Evaluator(vf, new PrintWriter(System.err), new ModuleEnvironment("***shell***"));
			interpreter = new RascalScriptInterpreter(this, eval);
			interpreter.initialize();
			setInterpreter(interpreter);
			setPrompt(new ScriptConsolePrompt("rascal>", ">>>>>>>"));
			addPatternMatchListener(new JumpToSource());
		}
		
		public RascalConsole(IDebugger debugger){
			super("Rascal", CONSOLE_ID);
			Evaluator eval = new DebuggableEvaluator(vf, new PrintWriter(System.err), new ModuleEnvironment("***shell***"),debugger);
			interpreter = new RascalScriptInterpreter(this, eval);
			interpreter.initialize();
			setInterpreter(interpreter);
			setPrompt(new ScriptConsolePrompt("rascal>", ">>>>>>>"));
			addPatternMatchListener(new JumpToSource());
		}
		
		
		public RascalScriptInterpreter getInterpreter() {
			return interpreter;			
		}

		public ScriptConsoleViewer getViewer(){
			return (ScriptConsoleViewer) page.getViewer();
		}

	}


}



