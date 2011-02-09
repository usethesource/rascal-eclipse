package org.rascalmpl.eclipse.console;

import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.runtime.RuntimePlugin;
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
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.TreeAdapter;


public class ConsoleFactory{
	public final static String INTERACTIVE_CONSOLE_ID = InteractiveInterpreterConsole.class.getName();
	private static final String SHELL_MODULE = "***shell***";

	private final static IValueFactory vf = ValueFactoryFactory.getValueFactory();
	private final static IConsoleManager fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
	
	private final PrintWriter stderr = new PrintWriter(RuntimePlugin.getInstance().getConsoleStream());
	private final PrintWriter stdout = stderr;
	
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
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new InteractiveRascalConsole(new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openRunConsole(IProject project){
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new InteractiveRascalConsole(project, new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public void openRunConsole(IProject project, IFile file) {
		IRascalConsole console = openRunConsole(project);
		Evaluator eval = console.getRascalInterpreter().getEval();
		try {
			IConstructor tree = eval.parseModule(file.getLocationURI(), new ModuleEnvironment("***tmp***", eval.getHeap()));
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
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new OutputRascalConsole(new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openRunOutputConsole(IProject project){
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new OutputRascalConsole(project, new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}

	public IRascalConsole openDebuggableConsole(IDebugger debugger){
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new InteractiveRascalConsole(debugger, new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openDebuggableConsole(IProject project, IDebugger debugger){
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new InteractiveRascalConsole(project, debugger, new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}

	public IRascalConsole openDebuggableOutputConsole(IDebugger debugger){
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new OutputRascalConsole(debugger, new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openDebuggableOutputConsole(IProject project, IDebugger debugger){
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new OutputRascalConsole(project, debugger, new ModuleEnvironment(SHELL_MODULE, heap), heap);
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
		public InteractiveRascalConsole(ModuleEnvironment shell, GlobalEnvironment heap){
			super(new RascalScriptInterpreter(), "Rascal", "rascal>", ">>>>>>>");
			getInterpreter().initialize(new Evaluator(vf, stderr, new PrintWriter(getConsoleOutputStream()), shell, heap));
			initializeConsole();
		}
		

		/* 
		 * console associated to a given Eclipse project 
		 * used to initialize the path with modules accessible 
		 * from the selected project and all its referenced projects
		 * */
		public InteractiveRascalConsole(IProject project, ModuleEnvironment shell, GlobalEnvironment heap){
			super(new RascalScriptInterpreter(project), "Rascal ["+project.getName()+"]", "rascal>", ">>>>>>>");
			getInterpreter().initialize(new Evaluator(vf, stderr, new PrintWriter(getConsoleOutputStream()), shell, heap));
			initializeConsole();
		}

		public InteractiveRascalConsole(IDebugger debugger, ModuleEnvironment shell, GlobalEnvironment heap){
			super(new RascalScriptInterpreter(), "Rascal", "rascal>", ">>>>>>>");
			getInterpreter().initialize(new DebuggableEvaluator(vf, stderr, new PrintWriter(getConsoleOutputStream()),  shell, debugger, heap));
			initializeConsole();
		}
		
		
		public InteractiveRascalConsole(IProject project, IDebugger debugger, ModuleEnvironment shell, GlobalEnvironment heap){
			super(new RascalScriptInterpreter(project), "Rascal ["+project.getName()+"]", "rascal>", ">>>>>>>");
			getInterpreter().initialize(new DebuggableEvaluator(vf, new PrintWriter(RuntimePlugin.getInstance().getConsoleStream()), new PrintWriter(getConsoleOutputStream()), shell, debugger, heap));
			initializeConsole();
		}
		

		public RascalScriptInterpreter getRascalInterpreter(){
			return (RascalScriptInterpreter) getInterpreter();
		}
	}

	private class OutputRascalConsole extends OutputInterpreterConsole implements IRascalConsole{
		public OutputRascalConsole(ModuleEnvironment shell, GlobalEnvironment heap){
			this(new Evaluator(vf, stderr, stdout,  shell, heap ));
		}

		public OutputRascalConsole(IDebugger debugger, ModuleEnvironment shell, GlobalEnvironment heap){
			this(new DebuggableEvaluator(vf, stderr, stdout,  shell, debugger, heap));
		}

		private OutputRascalConsole(Evaluator eval, IProject project){
			super(new RascalScriptInterpreter(project), "Rascal ["+project.getName()+"]");
			initializeConsole();
			getInterpreter().initialize(eval);
		}
	
		private OutputRascalConsole(Evaluator eval){
			super(new RascalScriptInterpreter(), "Rascal");
			initializeConsole();
			getInterpreter().initialize(eval);
		}

		public OutputRascalConsole(IProject project, IDebugger debugger,
				ModuleEnvironment shell, GlobalEnvironment heap) {
			this(new DebuggableEvaluator(vf, stderr, stdout,  shell, debugger, heap), project);
		}

		public OutputRascalConsole(IProject project,
				ModuleEnvironment shell, GlobalEnvironment heap) {
			this(new Evaluator(vf, stderr, stdout,  shell, heap), project);
		}
 
		public RascalScriptInterpreter getRascalInterpreter(){
			return (RascalScriptInterpreter) getInterpreter();
		}
	}

	


}



