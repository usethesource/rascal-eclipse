package org.meta_environment.rascal.eclipse.console;

import java.io.PrintWriter;

import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.meta_environment.ValueFactoryFactory;
import org.meta_environment.rascal.eclipse.console.internal.InterpreterConsole;
import org.meta_environment.rascal.interpreter.CommandEvaluator;
import org.meta_environment.rascal.interpreter.DebuggableEvaluator;
import org.meta_environment.rascal.interpreter.Evaluator;
import org.meta_environment.rascal.interpreter.IDebugger;
import org.meta_environment.rascal.interpreter.env.GlobalEnvironment;
import org.meta_environment.rascal.interpreter.env.ModuleEnvironment;
import org.meta_environment.rascal.parser.ConsoleParser;

public class ConsoleFactory implements IConsoleFactory {
	public final static String CONSOLE_ID = "org.meta_environment.rascal.eclipse.console";
	
	private final static IValueFactory vf = ValueFactoryFactory.getValueFactory();
	private final static IConsoleManager fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
	
	public ConsoleFactory(){
		super();
	}
	
	private static class InstanceKeeper{
		private final static ConsoleFactory instance = new ConsoleFactory();
	}

	public static ConsoleFactory getInstance(){
		return InstanceKeeper.instance;
	}

	public void openConsole(){
		RascalConsole console = new RascalConsole();
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
	}
	
	public RascalConsole openDebuggableConsole(IDebugger debugger){
		RascalConsole console = new RascalConsole(debugger);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		
		return console;
	}

	public class RascalConsole extends InterpreterConsole{
		private static final String SHELL_MODULE = "***shell***";

		public RascalConsole(){
			this(new CommandEvaluator(vf, new PrintWriter(System.err), new ModuleEnvironment(SHELL_MODULE), new GlobalEnvironment(), new ConsoleParser()));
		}
		
		public RascalConsole(IDebugger debugger){
			this(new DebuggableEvaluator(vf, new PrintWriter(System.err), new ModuleEnvironment(SHELL_MODULE), new ConsoleParser(), debugger));
		}
		
		private RascalConsole(Evaluator eval) {
			super(new RascalScriptInterpreter(eval), "Rascal", "rascal>", ">>>>>>>");
			initializeConsole();
			getInterpreter().initialize();
			addPatternMatchListener(new JumpToSource());
		}
		
		public RascalScriptInterpreter getRascalInterpreter(){
			return (RascalScriptInterpreter) getInterpreter();
		}
	}
}



