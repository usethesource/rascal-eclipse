package org.rascalmpl.eclipse.console;

import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IHyperlink;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.console.internal.IInterpreterConsole;
import org.rascalmpl.eclipse.console.internal.InteractiveInterpreterConsole;
import org.rascalmpl.eclipse.console.internal.OutputInterpreterConsole;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.debug.DebuggableEvaluator;
import org.rascalmpl.interpreter.debug.IDebugger;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.staticErrors.SyntaxError;
import org.rascalmpl.parser.IRascalParser;
import org.rascalmpl.parser.LegacyRascalParser;
import org.rascalmpl.parser.NewRascalParser;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.TreeAdapter;


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
	
	public IRascalConsole openRunConsole(boolean newParser){
		IRascalConsole console = new InteractiveRascalConsole(new ModuleEnvironment(SHELL_MODULE), newParser ? new NewRascalParser() : new LegacyRascalParser());
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openRunConsole(IProject project, boolean newParser){
		IRascalConsole console = new InteractiveRascalConsole(project, newParser ? new NewRascalParser() : new LegacyRascalParser(), new ModuleEnvironment(SHELL_MODULE));
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public void openRunConsole(IProject project, IFile file, boolean newParser) {
		IRascalConsole console = openRunConsole(project, newParser);
		Evaluator eval = console.getRascalInterpreter().getEval();
		try {
			IConstructor tree = eval.parseModule(file.getLocationURI(), new ModuleEnvironment("***tmp***"));
			IConstructor top = (IConstructor) tree.get(0);
			IConstructor mod = (IConstructor) TreeAdapter.getArgs(top).get(1);
			IConstructor header = (IConstructor) TreeAdapter.getArgs(mod).get(0);
			IConstructor name = (IConstructor) TreeAdapter.getArgs(header).get(4);
			String module = org.rascalmpl.values.uptr.TreeAdapter.yield(name);
			eval.doImport(module);
			new PrintWriter(console.getConsoleOutputStream()).print("Imported module " + module + "\n");
		} catch (IOException e) {
			Activator.getInstance().logException("failed to import module in " + file.getName(), e);
		} catch (SyntaxError e) {
			// can happen
		}
		
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
		public InteractiveRascalConsole(ModuleEnvironment shell, IRascalParser parser){
			super(new RascalScriptInterpreter(new Evaluator(vf, stderr, stdout, parser, shell, new GlobalEnvironment())), "Rascal", "rascal>", ">>>>>>>");
			initializeConsole();
			getInterpreter().initialize();
			getInterpreter().setStdOut(new PrintWriter(getConsoleOutputStream()));
		}
		

		/* 
		 * console associated to a given Eclipse project 
		 * used to initialize the path with modules accessible 
		 * from the selected project and all its referenced projects
		 * */
		public InteractiveRascalConsole(IProject project, IRascalParser parser, ModuleEnvironment shell){
			super(new RascalScriptInterpreter(new Evaluator(vf, stderr, stdout, parser, shell, new GlobalEnvironment()), project), "Rascal ["+project.getName()+"]", "rascal>", ">>>>>>>");
			initializeConsole();
			getInterpreter().initialize();
			getInterpreter().setStdOut(new PrintWriter(getConsoleOutputStream()));
		}

		public InteractiveRascalConsole(IDebugger debugger, ModuleEnvironment shell){
			super(new RascalScriptInterpreter(new DebuggableEvaluator(vf, stderr, stdout,  shell, debugger)), "Rascal", "rascal>", ">>>>>>>");
			initializeConsole();
			getInterpreter().initialize();
			getInterpreter().setStdOut(new PrintWriter(getConsoleOutputStream()));
		}
		
		
		public InteractiveRascalConsole(IProject project, IDebugger debugger, ModuleEnvironment shell){
			super(new RascalScriptInterpreter(new DebuggableEvaluator(vf, new PrintWriter(System.err),  new PrintWriter(System.out), shell, debugger), project), "Rascal ["+project.getName()+"]", "rascal>", ">>>>>>>");
			initializeConsole();
			getInterpreter().initialize();
			getInterpreter().setStdOut(new PrintWriter(getConsoleOutputStream()));
		}
		

		public RascalScriptInterpreter getRascalInterpreter(){
			return (RascalScriptInterpreter) getInterpreter();
		}
	}

	private class OutputRascalConsole extends OutputInterpreterConsole implements IRascalConsole{
		public OutputRascalConsole(ModuleEnvironment shell){
			this(new Evaluator(vf, stderr, stdout,  new LegacyRascalParser(), shell, new GlobalEnvironment()));
		}

		public OutputRascalConsole(IDebugger debugger, ModuleEnvironment shell){
			this(new DebuggableEvaluator(vf, stderr, stdout,  shell, debugger));
		}

		private OutputRascalConsole(Evaluator eval, IProject project){
			super(new RascalScriptInterpreter(eval, project), "Rascal ["+project.getName()+"]");
			initializeConsole();
			getInterpreter().initialize();
		}
	
		private OutputRascalConsole(Evaluator eval){
			super(new RascalScriptInterpreter(eval), "Rascal");
			initializeConsole();
			getInterpreter().initialize();
		}

		public OutputRascalConsole(IProject project, IDebugger debugger,
				ModuleEnvironment shell) {
			this(new DebuggableEvaluator(vf, stderr, stdout,  shell, debugger), project);
		}

		public OutputRascalConsole(IProject project,
				ModuleEnvironment shell) {
			this(new Evaluator(vf, stderr, stdout,  new LegacyRascalParser(), shell, new GlobalEnvironment()), project);
		}
 
		public RascalScriptInterpreter getRascalInterpreter(){
			return (RascalScriptInterpreter) getInterpreter();
		}
	}

	


}



