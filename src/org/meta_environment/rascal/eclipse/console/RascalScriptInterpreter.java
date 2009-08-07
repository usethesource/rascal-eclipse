package org.meta_environment.rascal.eclipse.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
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
import org.meta_environment.ValueFactoryFactory;
import org.meta_environment.errors.SummaryAdapter;
import org.meta_environment.rascal.ast.ASTFactory;
import org.meta_environment.rascal.ast.Command;
import org.meta_environment.rascal.ast.Expression;
import org.meta_environment.rascal.ast.NullASTVisitor;
import org.meta_environment.rascal.ast.Command.Ambiguity;
import org.meta_environment.rascal.ast.Command.Declaration;
import org.meta_environment.rascal.ast.Command.Import;
import org.meta_environment.rascal.ast.Command.Shell;
import org.meta_environment.rascal.ast.Command.Statement;
import org.meta_environment.rascal.ast.ShellCommand.Edit;
import org.meta_environment.rascal.ast.ShellCommand.Help;
import org.meta_environment.rascal.ast.ShellCommand.History;
import org.meta_environment.rascal.ast.ShellCommand.Quit;
import org.meta_environment.rascal.ast.ShellCommand.Test;
import org.meta_environment.rascal.eclipse.Activator;
import org.meta_environment.rascal.eclipse.console.internal.CommandExecutionException;
import org.meta_environment.rascal.eclipse.console.internal.CommandHistory;
import org.meta_environment.rascal.eclipse.console.internal.IInterpreter;
import org.meta_environment.rascal.eclipse.console.internal.InterpreterConsole;
import org.meta_environment.rascal.eclipse.console.internal.TerminationException;
import org.meta_environment.rascal.interpreter.DebuggableEvaluator;
import org.meta_environment.rascal.interpreter.Evaluator;
import org.meta_environment.rascal.interpreter.control_exceptions.FailedTestError;
import org.meta_environment.rascal.interpreter.control_exceptions.QuitException;
import org.meta_environment.rascal.interpreter.control_exceptions.Throw;
import org.meta_environment.rascal.interpreter.load.FromResourceLoader;
import org.meta_environment.rascal.interpreter.staticErrors.StaticError;
import org.meta_environment.rascal.parser.ASTBuilder;
import org.meta_environment.rascal.std.IO;
import org.meta_environment.uptr.Factory;
import org.meta_environment.uptr.TreeAdapter;

public class RascalScriptInterpreter implements IInterpreter{
	private final Evaluator eval;
	private volatile InterpreterConsole console;
	private String command;
	private String content;
	private IFile lastMarked;

	public RascalScriptInterpreter(Evaluator eval){
		super();
		
		this.command = "";
		this.eval = eval;

		eval.addModuleLoader(new ProjectModuleLoader());
		eval.addModuleLoader(new FromResourceLoader(RascalScriptInterpreter.class, "org/meta_environment/rascal/eclipse/lib"));
		
		eval.addSdfSearchPathContributor(new ProjectSDFModuleContributor());
		
		eval.addClassLoader(getClass().getClassLoader());
	}
	
	public void initialize(){
		loadCommandHistory();
	}
	
	public void setConsole(InterpreterConsole console){
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
	}

	public boolean execute(String cmd) throws CommandExecutionException, TerminationException{
		if(cmd.trim().length() == 0){
			content = "cancelled\n";
			command = "";
			return true;
		}

		try{
			command += cmd;

			IConstructor tree = eval.parseCommand(command);

			Type constructor = tree.getConstructorType();

			if (constructor == Factory.ParseTree_Summary) {
				execParseError(tree);
				return false;
			}
			
			IO.setOutputStream(new PrintStream(console.getConsoleOutputStream())); // Set output collector.
			
			execCommand(tree);
		}catch (StaticError e) {
			content = e.getMessage() + "\n";
			command = "";
			setMarker(e.getMessage(), e.getLocation());
			e.printStackTrace();
		}catch (Throw e) {
			content = e.getMessage() + "\n";
			String trace = e.getTrace();
			if (trace != null) {
				content += "stacktrace:\n" + trace;
			}
			command = "";
			setMarker(e.getMessage(), e.getLocation());
		}catch(QuitException q){
			throw new TerminationException();
		}catch(Throwable e){
			content = "internal exception: " + e.toString() + "\n";
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

			URL url = loc.getURL();

			if (url.getAuthority().equals("console")) {
				return;
			}

			lastMarked = new ProjectModuleLoader().getFile(url.getAuthority() + url.getPath());

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
			Activator.getInstance().logException("marker", ex);
		}
	}

	private void execCommand(IConstructor tree) {
		Command stat = new ASTBuilder(new ASTFactory()).buildCommand(tree);

		clearErrorMarker();

		IValue value = stat.accept(new NullASTVisitor<IValue>() {
			private Type TestReportType;

			@Override
			public IValue visitCommandStatement(Statement x) {
				return eval.eval(x.getStatement()).getValue();
			}

			@Override
			public IValue visitCommandDeclaration(Declaration x) {
				return eval.eval(x.getDeclaration()).getValue();
			}

			@Override
			public IValue visitCommandImport(Import x) {
				return eval.eval(x.getImported());
			}

			@Override
			public IValue visitCommandAmbiguity(Ambiguity x) {
				return null;
			}

			@Override
			public IValue visitCommandShell(Shell x) {
				return x.getCommand().accept(this);
			}

			@Override
			public IValue visitShellCommandEdit(Edit x) {
				try {
					editCommand(x);
				} catch (IOException e) {
					Activator.getInstance().logException("edit", e);
				} catch (CoreException e) {
					Activator.getInstance().logException("edit", e);
				}

				return null;
			}

			@Override
			public IValue visitShellCommandHelp(Help x) {
				return null;
			}

			@Override
			public IValue visitShellCommandHistory(History x) {
				return historyCommand();
			}

			@Override
			public IValue visitShellCommandQuit(Quit x) {
				saveCommandHistory();
				throw new QuitException();
			}
			
			@Override
			public IValue visitShellCommandTest(Test x) {
				List<FailedTestError> report = eval.runTests();
				return ValueFactoryFactory.getValueFactory().string(eval.report(report));
			}
		});
		
		if (value != null) {
			Type type = value.getType();
			if (type.isAbstractDataType() && type.isSubtypeOf(Factory.Tree)) {
				content = "[|" + new TreeAdapter((IConstructor) value).yield() + "|]\n" + 
					type + ": " + value.toString().substring(0, 50) + "...\n";
			}
			else {
				content = value.getType() + ": " + value.toString() + "\n";
			}
		} else {
			content = "ok\n";
		}

		if (eval instanceof DebuggableEvaluator) {
			// need to notify the debugger that the command is finished
			//DebuggableEvaluator debugEval = (DebuggableEvaluator) eval;
//			debugEval.getDebugger().stopStepping();
		}
		command = "";
	}

	private void editCommand(Edit x) throws IOException, CoreException {
		String module = x.getName().toString();
		final IFile file = new ProjectModuleLoader().getFile(module);
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

	private void execParseError(IConstructor tree) {
		ISourceLocation range = new SummaryAdapter(tree).getInitialSubject().getLocation();
		String[] commandLines = command.split("\n");
		int lastLine = commandLines.length;
		int lastColumn = commandLines[lastLine - 1].length();

		if (range.getEndLine() == lastLine && lastColumn <= range.getEndColumn()) { 
			content = "";
		}
		else {
			content = "";
			for (int i = 0; i < range.getEndColumn(); i++) {
				content += " ";
			}
			content += "^\nparse error at line " + lastLine + ", column " + range.getEndColumn() + "\n";
			command = "";
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
		try{
			File historyFile = getHistoryFile();
			BufferedReader in = new BufferedReader(new FileReader(historyFile));
			CommandHistory history = console.getHistory();

			String command = null;
			while((command = in.readLine()) != null){
				history.addToHistory(command);
			}

			in.close();
		}catch(IOException e){
			e.printStackTrace();
			Activator.getInstance().logException("history", e);
		}catch(Throwable t){
			t.printStackTrace();
		}
	}

	private void saveCommandHistory(){
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

	private IValue historyCommand(){
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
}
