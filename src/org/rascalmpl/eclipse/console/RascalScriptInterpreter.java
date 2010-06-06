package org.rascalmpl.eclipse.console;

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
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
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
import org.rascalmpl.ast.ASTFactory;
import org.rascalmpl.ast.Command;
import org.rascalmpl.ast.NullASTVisitor;
import org.rascalmpl.ast.Command.Shell;
import org.rascalmpl.ast.ShellCommand.Edit;
import org.rascalmpl.ast.ShellCommand.History;
import org.rascalmpl.ast.ShellCommand.Quit;
import org.rascalmpl.ast.ShellCommand.Test;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.internal.CommandExecutionException;
import org.rascalmpl.eclipse.console.internal.CommandHistory;
import org.rascalmpl.eclipse.console.internal.IInterpreter;
import org.rascalmpl.eclipse.console.internal.IInterpreterConsole;
import org.rascalmpl.eclipse.console.internal.TerminationException;
import org.rascalmpl.eclipse.console.internal.TestReporter;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.control_exceptions.QuitException;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.debug.DebuggableEvaluator;
import org.rascalmpl.interpreter.load.IRascalSearchPathContributor;
import org.rascalmpl.interpreter.result.Result;
import org.rascalmpl.interpreter.result.ResultFactory;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.interpreter.staticErrors.SyntaxError;
import org.rascalmpl.library.IO;
import org.rascalmpl.parser.ASTBuilder;
import org.rascalmpl.uri.ClassResourceInputStreamResolver;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.TreeAdapter;

public class RascalScriptInterpreter implements IInterpreter{
	private final static int LINE_LIMIT = 200;
	
	private final Evaluator eval;
	private volatile IInterpreterConsole console;
	private String command;
	private String content;
	private IFile lastMarked;

	private IProject project;
	
	public RascalScriptInterpreter(Evaluator eval, final IProject project){
		this(eval);
		this.project = project;
//		eval.addRascalSearchPath(URI.create("project://" + project.getName() + "/" + IRascalResources.RASCAL_SRC));
		eval.addRascalSearchPathContributor(new IRascalSearchPathContributor() {
			public void contributePaths(List<URI> path) {
				path.add(0,URI.create("project://" + project.getName() + "/" + IRascalResources.RASCAL_SRC));
			}
		});
		eval.addSdfSearchPathContributor(new ProjectSDFModuleContributor(project));
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new RascalModuleUpdateListener(this));
	}
	
	public void moduleUpdated(URI uri) {
		String path = uri.getPath();
		path = path.substring(0, path.indexOf(IRascalResources.RASCAL_EXT) - 1);
		path = path.startsWith("/") ? path.substring(1) : path;
		eval.reloadModule(path.replaceAll("/","::"));
	}
	
	public RascalScriptInterpreter(Evaluator eval){
		super();

		this.command = "";
		this.eval = eval;
		this.project = null;
		
		URIResolverRegistry.getInstance().registerInput("eclipse-lib", new ClassResourceInputStreamResolver("eclipse-lib", RascalScriptInterpreter.class));
		eval.addRascalSearchPath(URI.create("eclipse-lib:///org/rascalmpl/eclipse/library"));
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
			final IConstructor tree = eval.parseCommand(command, URI.create("stdin:///"));
			
			// TODO?? what is this doing here in the main loop?
			Object ioInstance = eval.getJavaBridge().getJavaClassInstance(IO.class);
			((IO) ioInstance).setOutputStream(new PrintStream(console.getConsoleOutputStream())); // Set output collector.

			// This code makes sure that if files are read or written during Rascal execution, the build
			// infra-structure of Eclipse is not triggered
			IWorkspaceRunnable action = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					monitor.beginTask("Rascal Command", IProgressMonitor.UNKNOWN);
					execCommand(tree);
					monitor.done();
				}
			};
			project.getWorkspace().run(action, project, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		}
		catch(SyntaxError e) {
			execParseError(e);
			return false;
		}
		catch(QuitException q){
			throw new TerminationException();
		}
		catch(StaticError e){
			content = e.getMessage();
			command = "";
			ISourceLocation location = e.getLocation();
			e.printStackTrace();
				setMarker(e.getMessage(), location);
				throw new CommandExecutionException(content, location.getOffset(), location.getLength());
		}
		catch(Throw e){
			content = e.getMessage() + "\n";
			String trace = e.getTrace();
			if (trace != null) {
				content += "stacktrace:\n" + trace;
			}
			command = "";
			ISourceLocation location = e.getLocation();
			if(location != null && !location.getURI().getScheme().equals("stdin")){
				setMarker(e.getMessage(), location);
				throw new CommandExecutionException(content, location.getOffset(), location.getLength());
			}
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

			if (project != null && url.getScheme().equals("project")) {
				lastMarked = project.getFile(url.getPath());

				if (lastMarked != null) {
					IMarker m = lastMarked.createMarker(IMarker.PROBLEM);

					m.setAttribute(IMarker.TRANSIENT, true);
					m.setAttribute(IMarker.CHAR_START, loc.getOffset());
					m.setAttribute(IMarker.CHAR_END, loc.getOffset() + loc.getLength());
					m.setAttribute(IMarker.MESSAGE, message);
					m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
					m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				}
			}
		} catch (CoreException ex) {
			Activator.getInstance().logException("marker", ex);
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
			
			@Override
			public Result<IValue> visitShellCommandTest(final Test x) {
				eval.setTestResultListener(new TestReporter());
				x.accept(eval);								
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
		
		if (project == null) {
			return;
			
		}
		final IFile file = project.getFile(module);
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

	private void execParseError(SyntaxError e) throws CommandExecutionException{
		if (e.getLocation().getURI().getScheme().equals("stdin")) {
			ISourceLocation location = e.getLocation();
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
		else {
			content = e.getMessage();
			command = "";
			ISourceLocation location = e.getLocation();
			e.printStackTrace();
				setMarker(e.getMessage(), location);
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

	public void setStdErr(PrintWriter w) {
		eval.setStdErr(w);
	}


	public void setStdOut(PrintWriter w) {
		eval.setStdOut(w);  
	}
}
