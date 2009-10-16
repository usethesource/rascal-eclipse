package org.meta_environment.rascal.eclipse.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.part.FileEditorInput;
import org.meta_environment.errors.SubjectAdapter;
import org.meta_environment.errors.SummaryAdapter;
import org.meta_environment.rascal.ast.ASTFactory;
import org.meta_environment.rascal.ast.Command;
import org.meta_environment.rascal.ast.Expression;
import org.meta_environment.rascal.ast.NullASTVisitor;
import org.meta_environment.rascal.ast.Command.Shell;
import org.meta_environment.rascal.ast.ShellCommand.Edit;
import org.meta_environment.rascal.ast.ShellCommand.History;
import org.meta_environment.rascal.ast.ShellCommand.Quit;
import org.meta_environment.rascal.eclipse.Activator;
import org.meta_environment.rascal.eclipse.console.internal.CommandExecutionException;
import org.meta_environment.rascal.eclipse.console.internal.CommandHistory;
import org.meta_environment.rascal.eclipse.console.internal.IInterpreter;
import org.meta_environment.rascal.eclipse.console.internal.IInterpreterConsole;
import org.meta_environment.rascal.eclipse.console.internal.TerminationException;
import org.meta_environment.rascal.interpreter.Evaluator;
import org.meta_environment.rascal.interpreter.control_exceptions.QuitException;
import org.meta_environment.rascal.interpreter.control_exceptions.Throw;
import org.meta_environment.rascal.interpreter.debug.DebuggableEvaluator;
import org.meta_environment.rascal.interpreter.load.FromResourceLoader;
import org.meta_environment.rascal.interpreter.result.Result;
import org.meta_environment.rascal.interpreter.result.ResultFactory;
import org.meta_environment.rascal.interpreter.staticErrors.StaticError;
import org.meta_environment.rascal.parser.ASTBuilder;
import org.meta_environment.rascal.library.IO;
import org.meta_environment.uptr.Factory;
import org.meta_environment.uptr.TreeAdapter;

public class RascalScriptInterpreter implements IInterpreter{
	private final static int LINE_LIMIT = 200;
	
	private final Evaluator eval;
	private volatile IInterpreterConsole console;
	private String command;
	private String content;
	private IFile lastMarked;
	/* module loader associated to a given Eclipse project */
	/* used for hyperlinks */
	private ProjectModuleLoader moduleLoader;
	
	public RascalScriptInterpreter(Evaluator eval, IProject project){
		this(eval);
		// initialize to the moduleLoader associated to a project
		moduleLoader = new ProjectModuleLoader(project);
		eval.addModuleLoader(moduleLoader);
		eval.addSdfSearchPathContributor(new ProjectSDFModuleContributor(project));
	}
	
	
	public IFile getFile(String fileName) throws IOException, CoreException {
		return moduleLoader.getFile(fileName);
	}

	public RascalScriptInterpreter(Evaluator eval){
		super();

		this.command = "";
		this.eval = eval;

		eval.addModuleLoader(new FromResourceLoader(RascalScriptInterpreter.class, "org/meta_environment/rascal/eclipse/library"));
		eval.addClassLoader(getClass().getClassLoader());
	}

	public void initialize(){
		loadCommandHistory();
	}

	public void setConsole(IInterpreterConsole console){
		this.console = console;
	}

	public void storeHistory(CommandHistory history){
		saveCommandHistory();
	}

	public void terminate(){
		saveCommandHistory();
		content = null;
		clearErrorMarker();
		ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] {console});
		if (eval instanceof DebuggableEvaluator) ((DebuggableEvaluator) eval).getDebugger().destroy();

	}

	public boolean execute(String cmd) throws CommandExecutionException, TerminationException{
		if(cmd.trim().length() == 0){
			content = "cancelled";
			command = "";
			return true;
		}

		try{
			command += cmd;
			IConstructor tree = eval.parseCommand(command);
			Type constructor = tree.getConstructorType();
			if(constructor == Factory.ParseTree_Summary) {
				execParseError(tree);
				return false;
			}

			IO.setOutputStream(new PrintStream(console.getConsoleOutputStream())); // Set output collector.
			execCommand(tree);
		}
		catch(QuitException q){
			throw new TerminationException();
		}
		catch(StaticError e){
			content = e.getMessage();
			command = "";
			ISourceLocation location = e.getLocation();
			if(location != null && location.getURI().getAuthority().equals("-")){
				setMarker(e.getMessage(), location);
				throw new CommandExecutionException(content, location.getOffset(), location.getLength());
			}
		}
		catch(Throw e){
			content = e.getMessage() + "\n";
			String trace = e.getTrace();
			if (trace != null) {
				content += "stacktrace:\n" + trace;
			}
			command = "";
			ISourceLocation location = e.getLocation();
			if(location != null && location.getURI().getAuthority().equals("-")){
				setMarker(e.getMessage(), location);
				throw new CommandExecutionException(content, location.getOffset(), location.getLength());
			}
		}
		catch (CommandExecutionException e) {
			throw e;
		}
		catch(Throwable e){
			content = "internal exception: " + e.toString();
			e.printStackTrace();
			command = "";
		}
		return true;
	}

	private void setMarker(String message, ISourceLocation loc) {
		try {
			if (loc == null) {
				return;
			}

			URI url = loc.getURI();

			if (url.getScheme().equals("console")) {
				return;
			}

			lastMarked = getFile(url.getPath());

			if (lastMarked != null) {
				IMarker m = lastMarked.createMarker(IMarker.PROBLEM);

				m.setAttribute(IMarker.TRANSIENT, true);
				m.setAttribute(IMarker.CHAR_START, loc.getOffset());
				m.setAttribute(IMarker.CHAR_END, loc.getOffset() + loc.getLength());
				m.setAttribute(IMarker.MESSAGE, message);
				m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
				m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			}
		} catch (CoreException ex) {
			Activator.getInstance().logException("marker", ex);
		} catch (IOException ex) {
			//Activator.getInstance().logException("marker", ex);
			// Ignore, can happen.
		}
		
	}

	private void execCommand(IConstructor tree) {
		Command stat = new ASTBuilder(new ASTFactory()).buildCommand(tree);

		clearErrorMarker();

		// We first try to evaluate commands that have specific implementations in the
		// Eclipse environment (such as editing a file). After that we simply call
		// the evaluator to reuse as much of the evaluators standard implementation of commands
		
		Result<IValue> result = stat.accept(new NullASTVisitor<Result<IValue>>() {
			@Override
			public Result<IValue> visitCommandShell(Shell x) {
				return x.getCommand().accept(this);
			}
			
			@Override
			public Result<IValue> visitShellCommandEdit(Edit x) {
				try {
					editCommand(x);
				} catch (IOException e) {
					Activator.getInstance().logException("edit", e);
				} catch (CoreException e) {
					Activator.getInstance().logException("edit", e);
				}

				return ResultFactory.nothing();
			}

			public Result<IValue> visitShellCommandHistory(History x) {
				return historyCommand();
			}

			@Override
			public Result<IValue> visitShellCommandQuit(Quit x) {
				saveCommandHistory();
				throw new QuitException();
			}
		});
		
		if (result == null) {
			result = stat.accept(eval);
		}
		
		IValue value = result.getValue();
		if (value != null) {
			Type type = result.getType();
			if (type.isAbstractDataType() && type.isSubtypeOf(Factory.Tree)) {
				content = "`" + limitString(TreeAdapter.yield((IConstructor) value)) + "`\n" + 
				result.toString(LINE_LIMIT);
			} else {
				content = result.toString(LINE_LIMIT);
			}
		} else {
			content = "ok";
		}

		if (eval instanceof DebuggableEvaluator) {
			// need to notify the debugger that the command is finished
			DebuggableEvaluator debugEval = (DebuggableEvaluator) eval;
			debugEval.getDebugger().stopStepping();
		}
		command = "";
	}
	
	private static String limitString(String valString){
		return (valString.length() <= 200) ? valString : valString.substring(0, 200 - 3) + "...";
	}
	
	private void editCommand(Edit x) throws IOException, CoreException {
		String module = x.getName().toString();
		final IFile file = getFile(module);
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		
		if (win != null) {
			final IWorkbenchPage page = win.getActivePage();

			if (page != null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							page.openEditor(new FileEditorInput(file), UniversalEditor.EDITOR_ID);
						} catch (PartInitException e) {
							Activator.getInstance().logException("edit", e);
						}
					}
				});
			}
		}
	}

	private void clearErrorMarker() {
		if (lastMarked != null) {
			try {
				lastMarked.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				Activator.getInstance().logException("marker", e);
			}
		}
	}

	private void execParseError(IConstructor tree) throws CommandExecutionException{
		SubjectAdapter initialSubject = new SummaryAdapter(tree).getInitialSubject();
		
		if (initialSubject == null) {
			throw new CommandExecutionException("parse error at unknown location");
		}
		
		ISourceLocation location = initialSubject.getLocation();
		String[] commandLines = command.split("\n");
		int lastLine = commandLines.length;
		int lastColumn = commandLines[lastLine - 1].length();

		if (location.getEndLine() == lastLine && lastColumn <= location.getEndColumn()) { 
			content = "";
		} else {
			content = "";
			for (int i = 0; i < location.getEndColumn() + "rascal>".length(); i++) {
				
				content += " ";
			}
			content += "^ ";
			content += "parse error here";
			command = "";
			throw new CommandExecutionException(content, location.getOffset(), location.getLength());
		}
	}

	public String getOutput(){
		String output = content;
		content = "";
		return output;
	}

	@Override
	protected void finalize() throws Throwable{
		clearErrorMarker();
		saveCommandHistory();
	}

	private void loadCommandHistory(){
		if(console.hasHistory()){
			CommandHistory history = console.getHistory();
			BufferedReader in = null;
			try{
				File historyFile = getHistoryFile();
				in = new BufferedReader(new FileReader(historyFile));

				String command = null;
				while((command = in.readLine()) != null){
					history.addToHistory(command);
				}
			}catch(IOException e){
				e.printStackTrace();
				Activator.getInstance().logException("history", e);
			}catch(Throwable t){
				t.printStackTrace();
			}finally{
				if(in != null){
					try{
						in.close();
					}catch(IOException e){
						Activator.getInstance().logException("history", e);
					}
				}
			}
		}
	}

	private void saveCommandHistory(){
		if(console.hasHistory()){
			CommandHistory history = console.getHistory();
			OutputStream out = null; 
			try{
				File historyFile = getHistoryFile();

				out = new FileOutputStream(historyFile);
				do{/* Nothing */}while(history.getPreviousCommand() != "");

				String command;
				while((command = history.getNextCommand()) != ""){
					out.write(command.getBytes());
					out.write('\n');
				}
			}catch(FileNotFoundException e){
				Activator.getInstance().logException("history", e);
			}catch(IOException e){
				Activator.getInstance().logException("history", e);
			}finally{
				if(out != null){
					try{
						out.close();
					}catch(IOException e){
						Activator.getInstance().logException("history", e);
					}
				}
			}
		}
	}

	private File getHistoryFile() throws IOException{
		File home = new File(System.getProperty("user.home"));
		File rascal = new File(home, ".rascal");
		if(!rascal.exists()){
			rascal.mkdirs();
		}
		File historyFile = new File(rascal, "history");
		if(!historyFile.exists()){
			historyFile.createNewFile();
		}
		return historyFile;
	}

	private Result<IValue> historyCommand(){
		return null;
	}

	public Evaluator getEval(){
		return eval;
	}

	/* construct an Expression AST from a String */ 
	public Expression getExpression(String expression) throws IOException{
		IConstructor tree = eval.parseCommand(expression+";");
		Command c = new ASTBuilder(new ASTFactory()).buildCommand(tree);	
		return c.getStatement().getExpression();
	}

  
	public void setStdErr(PrintWriter w) {
		eval.setStdErr(w);
	}


	public void setStdOut(PrintWriter w) {
		eval.setStdOut(w);  
	}
}
