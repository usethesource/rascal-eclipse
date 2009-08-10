package org.meta_environment.rascal.eclipse.console;

import java.io.PrintWriter;

import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleManager;
import org.meta_environment.ValueFactoryFactory;
import org.meta_environment.rascal.eclipse.console.internal.IInterpreterConsole;
import org.meta_environment.rascal.eclipse.console.internal.InteractiveInterpreterConsole;
import org.meta_environment.rascal.eclipse.console.internal.OutputInterpreterConsole;
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
	
	// TODO I'd like to get rid of this
	public void openConsole(){
		IRascalConsole console = new InteractiveRascalConsole();
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
	}

	public IRascalConsole openRunConsole(){
		IRascalConsole console = new InteractiveRascalConsole();
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}

	public IRascalConsole openRunOutputConsole(){
		IRascalConsole console = new OutputRascalConsole();
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}

	public IRascalConsole openDebuggableConsole(IDebugger debugger){
		IRascalConsole console = new InteractiveRascalConsole(debugger);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}

	public IRascalConsole openDebuggableOutputConsole(IDebugger debugger){
		IRascalConsole console = new OutputRascalConsole(debugger);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public interface IRascalConsole extends IInterpreterConsole{
		void activate(); // Eclipse thing.
		RascalScriptInterpreter getRascalInterpreter();
	}

	private class InteractiveRascalConsole extends InteractiveInterpreterConsole implements IRascalConsole{
		private static final String SHELL_MODULE = "***shell***";

		public InteractiveRascalConsole(){
			this(new CommandEvaluator(vf, new PrintWriter(System.err), new ModuleEnvironment(SHELL_MODULE), new GlobalEnvironment(), new ConsoleParser()));
		}

		public InteractiveRascalConsole(IDebugger debugger){
			this(new DebuggableEvaluator(vf, new PrintWriter(System.err), new ModuleEnvironment(SHELL_MODULE), new ConsoleParser(), debugger));
		}

		private InteractiveRascalConsole(Evaluator eval){
			super(new RascalScriptInterpreter(eval), "Rascal", "rascal>", ">>>>>>>");
			initializeConsole();
			getInterpreter().initialize();
			addPatternMatchListener(new JumpToSource());
		}

		public RascalScriptInterpreter getRascalInterpreter(){
			return (RascalScriptInterpreter) getInterpreter();
		}
	}

	private class OutputRascalConsole extends OutputInterpreterConsole implements IRascalConsole{
		private static final String SHELL_MODULE = "***shell***";

		public OutputRascalConsole(){
			this(new CommandEvaluator(vf, new PrintWriter(System.err), new ModuleEnvironment(SHELL_MODULE), new GlobalEnvironment(), new ConsoleParser()));
		}

		public OutputRascalConsole(IDebugger debugger){
			this(new DebuggableEvaluator(vf, new PrintWriter(System.err), new ModuleEnvironment(SHELL_MODULE), new ConsoleParser(), debugger));
		}

		private OutputRascalConsole(Evaluator eval){
			super(new RascalScriptInterpreter(eval), "Rascal");
			initializeConsole();
			getInterpreter().initialize();
			addPatternMatchListener(new JumpToSource());
		}

		public RascalScriptInterpreter getRascalInterpreter(){
			return (RascalScriptInterpreter) getInterpreter();
		}
	}
}



