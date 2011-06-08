/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Tijs van der Storm - Tijs.van.der.Storm@cwi.nl
 *   * Emilie Balland - (CWI)
 *   * Anya Helene Bagge - A.H.S.Bagge@cwi.nl (Univ. Bergen)
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
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
import org.rascalmpl.eclipse.nature.ModuleReloader;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IRascalMonitor;
import org.rascalmpl.interpreter.asserts.Ambiguous;
import org.rascalmpl.interpreter.asserts.ImplementationError;
import org.rascalmpl.interpreter.control_exceptions.InterruptException;
import org.rascalmpl.interpreter.control_exceptions.QuitException;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.debug.DebuggableEvaluator;
import org.rascalmpl.interpreter.result.Result;
import org.rascalmpl.interpreter.result.ResultFactory;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.interpreter.staticErrors.SyntaxError;
import org.rascalmpl.parser.ASTBuilder;
import org.rascalmpl.parser.gtd.exception.ParseError;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.TreeAdapter;

public class RascalScriptInterpreter extends Job implements IInterpreter {
	private final static int LINE_LIMIT = 200;
	private ModuleReloader reloader;
	private Evaluator eval;
	private volatile IInterpreterConsole console;
	private String command;
	private String content;
	private IFile lastMarked;
	private IProject project;
	private Throwable error = null;

	public RascalScriptInterpreter(IProject project){
		super("Rascal");
		
		this.project = project;

		this.command = "";
	}
	
	public RascalScriptInterpreter(){
		this(null);
	}

	public void initialize(Evaluator eval){
		ProjectEvaluatorFactory.getInstance().initializeProjectEvaluator(project, eval);
		loadCommandHistory();
		synchronized(eval){
			eval.doImport(null, "IO");
			eval.doImport(null, "ParseTree");
		}
		this.eval = eval;
		this.reloader = new ModuleReloader(eval);
	}

	public void setConsole(IInterpreterConsole console){
		this.console = console;
	}

	public void storeHistory(CommandHistory history){
		saveCommandHistory();
	}

	public void interrupt() {
		eval.interrupt();
	}
	
	public void terminate() {
		saveCommandHistory();
		content = null;
		clearErrorMarker();
		
		reloader.destroy();
		
		ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] {console});
		if (eval instanceof DebuggableEvaluator) ((DebuggableEvaluator) eval).getDebugger().destroy();
		
		// Make the memory leak less severe (Eclipse is broken, I can't help it).
		eval = null;
		reloader = null;
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			RascalMonitor rm = new RascalMonitor(monitor);
			rm.startJob("executing command", 10000);
			rm.event("parsing command");
			synchronized(eval){
				IConstructor tree = eval.parseCommand(rm, command, URI.create("stdin:///"));
				rm.event("running command");
				execCommand(rm, tree);
			}
			rm.endJob(true);
		
		}
		catch (ParseError e) {
			try {
				execParseError(e);
				error = e;
			} catch (CommandExecutionException e1) {
				error = e1;
			}
		}
		catch (QuitException q){
			error = new TerminationException();
		}
		catch(InterruptException i) {
			content = i.toString();
			command = "";
		}
		catch (Ambiguous e) {
			content = e.getMessage();
			command = "";
		}
		catch(StaticError e){
			content = e.getMessage();
			command = "";
			ISourceLocation location = e.getLocation();
			e.printStackTrace();
			if (location != null) {
				setMarker(e.getMessage(), location);
				error = new CommandExecutionException(content, location.getOffset(), location.getLength());
			}
			
			error = new CommandExecutionException(content);
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
				error = new CommandExecutionException(content, location.getOffset(), location.getLength());
			}
		}
		catch(Throwable e){
			content = "internal exception: " + e.toString();
			if (eval != null) {
				content += eval.getStackTrace();
			}
			e.printStackTrace();
			command = "";
		}
		
		return Status.OK_STATUS;
	}

	public synchronized boolean execute(String cmd) throws CommandExecutionException, TerminationException{
		if(cmd.trim().length() == 0){
			content = "cancelled";
			command = "";
			return true;
		}

		try {
			reloader.updateModules();
			command += cmd;
			
			project.getWorkspace().getRoot().deleteMarkers(IRascalResources.ID_RASCAL_MARKER_TYPE_TEST_RESULTS, false, IResource.DEPTH_INFINITE);
			error = null;
			schedule();
			join();
			if(error != null){
				if(error instanceof CommandExecutionException) {
					throw ((CommandExecutionException) error);
				}else if(error instanceof TerminationException){
					throw ((TerminationException) error);
				}
				
				return false;
			}
		} catch (CoreException e) {
			Activator.getInstance().logException("could not delete test markers", e);
		} catch (InterruptedException e) {
			eval.interrupt();
			command = "";
			content = "interrupted";
			eval.__setInterrupt(false);
		}
		
		return true;
	}
	
	private void setMarker(final String message, final ISourceLocation loc){
		// This code makes sure that if files are read or written during Rascal execution, the build
		// infra-structure of Eclipse is not triggered
		IWorkspaceRunnable action = new IWorkspaceRunnable(){
			public void run(IProgressMonitor monitor) throws CoreException{
				try{
					if(loc == null){
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
		};
		
		try{
			project.getWorkspace().run(action, project, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		}catch(CoreException cex){
			Activator.getInstance().logException("marker", cex);
		}
	}

	private void execCommand(IRascalMonitor monitor, IConstructor tree) {
		Command stat = new ASTBuilder().buildCommand(tree);

		if (stat == null) {
			throw new ImplementationError("null command");
		}
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
				editCommand(x);
				
				return ResultFactory.nothing();
			}
			
			@Override
			public Result<IValue> visitShellCommandTest(final Test x) {
				eval.setTestResultListener(new TestReporter());
				x.interpret(eval);								
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
			result = eval.eval(monitor, stat);
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
	
	private void editCommand(Edit x){
		String module = x.getName().toString();
		
		if (project == null) {
			return;
			
		}
		final IFile file = project.getFile(IRascalResources.RASCAL_SRC + "/" + module.replaceAll("::","/") + "." + IRascalResources.RASCAL_EXT);

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					IWorkbench wb = PlatformUI.getWorkbench();
					IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
					if (win == null) return;
					final IWorkbenchPage page = win.getActivePage();
					if (page == null) return;
					page.openEditor(new FileEditorInput(file), UniversalEditor.EDITOR_ID);
				} catch (PartInitException e) {
					Activator.getInstance().logException("edit", e);
				}
			}
		});
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

	private void execParseError(ParseError e) throws CommandExecutionException{
		if (e.getLocation().getURI().getScheme().equals("stdin")) {
			ISourceLocation location = e.getLocation();
			String[] commandLines = command.split("\n");
			int lastLine = commandLines.length;
			int lastColumn = commandLines[lastLine - 1].length();

			if (location.getEndLine() == lastLine && lastColumn <= location.getEndColumn()) { 
				content = "";
			} else {
				content = "";
				int i = 0;
				for ( ; i < location.getEndColumn() + "rascal>".length(); i++) {

					content += " ";
				}
				content += "^ ";
				content += "parse error here";
				if (i > 80) {
					content += "\nparse error at column " + location.getEndColumn();
				}
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

	public String getTrace() {
		return eval.getStackTrace();
	}

	
}
