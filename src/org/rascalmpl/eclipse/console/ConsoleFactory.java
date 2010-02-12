package org.rascalmpl.eclipse.console;

import java.io.PrintWriter;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IHyperlink;
import org.rascalmpl.eclipse.console.internal.IInterpreterConsole;
import org.rascalmpl.eclipse.console.internal.InteractiveInterpreterConsole;
import org.rascalmpl.eclipse.console.internal.OutputInterpreterConsole;
import org.rascalmpl.interpreter.CommandEvaluator;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.debug.DebuggableEvaluator;
import org.rascalmpl.interpreter.debug.IDebugger;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.parser.ConsoleParser;
import org.rascalmpl.values.ValueFactoryFactory;

public class ConsoleFactory{
	public final static String INTERACTIVE_CONSOLE_ID = InteractiveInterpreterConsole.class.getName();
	private static final String SHELL_MODULE = "***shell***";

	private final static IValueFactory vf = ValueFactoryFactory.getValueFactory();
	private final static IConsoleManager fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
	
	private final PrintWriter stderr = new PrintWriter(System.err);
	private final PrintWriter stdout = new PrintWriter(System.out);
	
	public ConsoleFactory(){
		super();
	}

	private static class InstanceKeeper{
		private final static ConsoleFactory instance = new ConsoleFactory();
	}

	public static ConsoleFactory getInstance(){
		return InstanceKeeper.instance;
	}
	
	public IRascalConsole openRunConsole(){
		IRascalConsole console = new InteractiveRascalConsole(new ModuleEnvironment(SHELL_MODULE));
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openRunConsole(IProject project){
		IRascalConsole console = new InteractiveRascalConsole(project, new ModuleEnvironment(SHELL_MODULE));
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}

	public IRascalConsole openRunOutputConsole(){
		IRascalConsole console = new OutputRascalConsole(new ModuleEnvironment(SHELL_MODULE));
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openRunOutputConsole(IProject project){
		IRascalConsole console = new OutputRascalConsole(project, new ModuleEnvironment(SHELL_MODULE));
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}

	public IRascalConsole openDebuggableConsole(IDebugger debugger){
		IRascalConsole console = new InteractiveRascalConsole(debugger, new ModuleEnvironment(SHELL_MODULE));
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openDebuggableConsole(IProject project, IDebugger debugger){
		IRascalConsole console = new InteractiveRascalConsole(project, debugger, new ModuleEnvironment(SHELL_MODULE));
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}

	public IRascalConsole openDebuggableOutputConsole(IDebugger debugger){
		IRascalConsole console = new OutputRascalConsole(debugger, new ModuleEnvironment(SHELL_MODULE));
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openDebuggableOutputConsole(IProject project, IDebugger debugger){
		IRascalConsole console = new OutputRascalConsole(project, debugger, new ModuleEnvironment(SHELL_MODULE));
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public interface IRascalConsole extends IInterpreterConsole{
		void activate(); // Eclipse thing.
		RascalScriptInterpreter getRascalInterpreter();
		IDocument getDocument();
	    void addHyperlink(IHyperlink hyperlink, int offset, int length) throws BadLocationException;
	}

	private class InteractiveRascalConsole extends InteractiveInterpreterConsole implements IRascalConsole{
		public InteractiveRascalConsole(ModuleEnvironment shell){
			super(new RascalScriptInterpreter(new CommandEvaluator(vf, stderr, stdout, shell, new GlobalEnvironment(), new ConsoleParser(shell))), "Rascal", "rascal>", ">>>>>>>");
			initializeConsole();
			getInterpreter().initialize();
			addPatternMatchListener(new JumpToSource());
			getInterpreter().setStdOut(new PrintWriter(getConsoleOutputStream()));
		}
		

		/* 
		 * console associated to a given Eclipse project 
		 * used to initialize the path with modules accessible 
		 * from the selected project and all its referenced projects
		 * */
		public InteractiveRascalConsole(IProject project, ModuleEnvironment shell){
			super(new RascalScriptInterpreter(new CommandEvaluator(vf, stderr, stdout, shell, new GlobalEnvironment(), new ConsoleParser(shell)), project), "Rascal ["+project.getName()+"]", "rascal>", ">>>>>>>");
			initializeConsole();
			getInterpreter().initialize();
			addPatternMatchListener(new JumpToSource());
			getInterpreter().setStdOut(new PrintWriter(getConsoleOutputStream()));
		}

		public InteractiveRascalConsole(IDebugger debugger, ModuleEnvironment shell){
			super(new RascalScriptInterpreter(new DebuggableEvaluator(vf, stderr, stdout,  shell, new ConsoleParser(shell), debugger)), "Rascal", "rascal>", ">>>>>>>");
			initializeConsole();
			getInterpreter().initialize();
			addPatternMatchListener(new JumpToSource());
			getInterpreter().setStdOut(new PrintWriter(getConsoleOutputStream()));
		}
		
		
		public InteractiveRascalConsole(IProject project, IDebugger debugger, ModuleEnvironment shell){
			super(new RascalScriptInterpreter(new DebuggableEvaluator(vf, new PrintWriter(System.err),  new PrintWriter(System.out), shell, new ConsoleParser(shell), debugger), project), "Rascal ["+project.getName()+"]", "rascal>", ">>>>>>>");
			initializeConsole();
			getInterpreter().initialize();
			addPatternMatchListener(new JumpToSource());
			getInterpreter().setStdOut(new PrintWriter(getConsoleOutputStream()));
		}
		

		public RascalScriptInterpreter getRascalInterpreter(){
			return (RascalScriptInterpreter) getInterpreter();
		}
	}

	private class OutputRascalConsole extends OutputInterpreterConsole implements IRascalConsole{
		public OutputRascalConsole(ModuleEnvironment shell){
			this(new CommandEvaluator(vf, stderr, stdout,  shell, new GlobalEnvironment(), new ConsoleParser(shell)));
		}

		public OutputRascalConsole(IDebugger debugger, ModuleEnvironment shell){
			this(new DebuggableEvaluator(vf, stderr, stdout,  shell, new ConsoleParser(shell), debugger));
		}

		private OutputRascalConsole(Evaluator eval, IProject project){
			super(new RascalScriptInterpreter(eval, project), "Rascal ["+project.getName()+"]");
			initializeConsole();
			getInterpreter().initialize();
			addPatternMatchListener(new JumpToSource());
		}
	
		private OutputRascalConsole(Evaluator eval){
			super(new RascalScriptInterpreter(eval), "Rascal");
			initializeConsole();
			getInterpreter().initialize();
			addPatternMatchListener(new JumpToSource());
		}

		public OutputRascalConsole(IProject project, IDebugger debugger,
				ModuleEnvironment shell) {
			this(new DebuggableEvaluator(vf, stderr, stdout,  shell, new ConsoleParser(shell), debugger), project);
		}

		public OutputRascalConsole(IProject project,
				ModuleEnvironment shell) {
			this(new CommandEvaluator(vf, stderr, stdout,  shell, new GlobalEnvironment(), new ConsoleParser(shell)), project);
		}
 
		public RascalScriptInterpreter getRascalInterpreter(){
			return (RascalScriptInterpreter) getInterpreter();
		}
	}


}



