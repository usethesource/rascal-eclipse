/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
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
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.console;

import static org.rascalmpl.interpreter.AbstractInterpreterEventTrigger.newNullEventTrigger;
import static org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages.ambiguousMessage;
import static org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages.interruptedExceptionMessage;
import static org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages.parseErrorMessage;
import static org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages.resultMessage;
import static org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages.staticErrorMessage;
import static org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages.throwMessage;
import static org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages.throwableMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;
import org.rascalmpl.ast.Command;
import org.rascalmpl.ast.Command.Shell;
import org.rascalmpl.ast.NullASTVisitor;
import org.rascalmpl.ast.ShellCommand.Edit;
import org.rascalmpl.ast.ShellCommand.History;
import org.rascalmpl.ast.ShellCommand.Quit;
import org.rascalmpl.ast.ShellCommand.Test;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.ambidexter.ReportView;
import org.rascalmpl.eclipse.console.internal.CommandExecutionException;
import org.rascalmpl.eclipse.console.internal.CommandHistory;
import org.rascalmpl.eclipse.console.internal.ConcurrentCircularOutputStream;
import org.rascalmpl.eclipse.console.internal.IInterpreter;
import org.rascalmpl.eclipse.console.internal.IInterpreterConsole;
import org.rascalmpl.eclipse.console.internal.PausableOutput;
import org.rascalmpl.eclipse.console.internal.TerminationException;
import org.rascalmpl.eclipse.console.internal.TestReporter;
import org.rascalmpl.eclipse.console.internal.TimedBufferedPipe;
import org.rascalmpl.eclipse.nature.ModuleReloader;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.util.ResourcesToModules;
import org.rascalmpl.interpreter.AbstractInterpreterEventTrigger;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IRascalMonitor;
import org.rascalmpl.interpreter.asserts.Ambiguous;
import org.rascalmpl.interpreter.asserts.ImplementationError;
import org.rascalmpl.interpreter.control_exceptions.InterruptException;
import org.rascalmpl.interpreter.control_exceptions.QuitException;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.result.Result;
import org.rascalmpl.interpreter.result.ResultFactory;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.interpreter.utils.Names;
import org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages;
import org.rascalmpl.parser.ASTBuilder;
import org.rascalmpl.parser.gtd.exception.ParseError;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.uptr.Factory;

public class RascalScriptInterpreter extends Job implements IInterpreter {
	private ModuleReloader reloader;
	private Evaluator eval;
	private volatile IInterpreterConsole console;
	private String command;
	private String content;
	private IFile lastMarked;
	private IProject project;
	private Throwable error = null;
	private PrintWriter consoleStdOut;
	private PrintWriter consoleStdErr;
	private TimedBufferedPipe consoleStreamPipe;
	private AbstractInterpreterEventTrigger eventTrigger;
	
	public RascalScriptInterpreter(IProject project){
		super("Rascal");
		
		this.eventTrigger = newNullEventTrigger();
		
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
			long before = System.currentTimeMillis();
			//eval.doImport(null, "Prelude"); String mods = "Prelude";
			eval.doImport(null, "IO");   String mods = "IO & ParseTree";
			eval.doImport(null, "ParseTree");
			long after = System.currentTimeMillis();
			System.err.println("RascalScriptInterpreter: Importing " + mods + " takes " + (after - before) + " msec.");
		}
		this.eval = eval;
		this.reloader = new ModuleReloader(eval);
	
		getEventTrigger().fireCreationEvent();
	}

	private void updateConsoleStream(IInterpreterConsole console) {
		final OutputStream target = console.getConsoleOutputStream();
		
		StringBuffer consoleStreamPipeName = new StringBuffer("Rascal console output syncer");
		if (this.project != null) { 
			consoleStreamPipeName.append(" [");
			consoleStreamPipeName.append(this.project.getName());
			consoleStreamPipeName.append("]");
		}
		
		consoleStreamPipe = new TimedBufferedPipe(50, new PausableOutput() {
			@Override
			public boolean isPaused() {
				return false;
			}

			@Override
			public void output(byte[] b) throws IOException {
				target.write(b);
			}
		}, consoleStreamPipeName.toString());
		ConcurrentCircularOutputStream fasterStream = new ConcurrentCircularOutputStream(4*1024*1024, consoleStreamPipe);
		consoleStreamPipe.initializeWithStream(fasterStream);
		try {
			// create buffer loop
			//this.consoleStdErr = new PrintWriter(new OutputStreamWriter(fasterStream, "UTF16"),true);
			this.consoleStdErr = null; // perhaps errors do not need to show up in the main console!
			this.consoleStdOut = new PrintWriter(new OutputStreamWriter(fasterStream, "UTF16"),false);
		} catch (UnsupportedEncodingException e) {
			Activator.getInstance().logException("could not get stderr/stdout writer", e);
		}
	}
	
	public void setConsole(IInterpreterConsole console){
		this.console = console;
		updateConsoleStream(console);
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
		getEventTrigger().fireTerminateEvent();
		
		// Make the memory leak less severe (Eclipse is broken, I can't help it).
		eval = null;
		reloader = null;
		if (consoleStdErr != null) {
			consoleStdErr.close();
			consoleStdErr = null;
		}
		if (consoleStdOut != null) {
			consoleStdOut.close();
			consoleStdOut = null;
		}
		if (consoleStreamPipe != null) {
			consoleStreamPipe.terminate();
			consoleStreamPipe = null;
		}
	}
	
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		RascalMonitor rm = new RascalMonitor(monitor);
		rm.startJob("executing command", 10000);
		rm.event("parsing command");
		synchronized (eval) {
			try {
				eval.overrideDefaultWriters(consoleStdOut, consoleStdErr);
				IConstructor tree = eval.parseCommand(rm, command, URIUtil.rootScheme("stdin"));
				rm.event("running command");
				reloader.updateModules(monitor);
				execCommand(rm, tree);
			}
			catch (ParseError e) {
				if (e.getLocation().getScheme().equals("stdin")) {
					if (e.getOffset() >= command.length()) {
						content = "";
						error = e;
					} else {
						content = parseErrorMessage(command, "stdin", e) + "\n";
						error = new CommandExecutionException(content,
								e.getOffset(), e.getLength());
						command = "";
					}
				} else {
					content = parseErrorMessage(command, "stdin", e) + "\n";
					error = new CommandExecutionException(content);
					command = "";
				}
			} catch (QuitException q) {
				error = new TerminationException();
			} catch (InterruptException i) {
				content = interruptedExceptionMessage(i) + "\n";
				command = "";
			} catch (Ambiguous e) {
				Activator.getInstance().logException(e.getMessage(), e);
				content = ambiguousMessage(e) + "\n";
				command = "";
			} catch (StaticError e) {
				content = staticErrorMessage(e) + "\n";
				command = "";
				ISourceLocation location = e.getLocation();
				if (location != null
						&& !location.getURI().getScheme().equals("stdin")) {
					setMarker(e.getMessage(), location.getURI(),
							location.getOffset(), location.getLength());
					error = new CommandExecutionException(content);
				} else if (location != null
						&& location.getURI().getScheme().equals("stdin")) {
					error = new CommandExecutionException(content,
							location.getOffset(), location.getLength());
				} else {
					error = new CommandExecutionException(content);
				}
			} catch (Throw e) {
				content = throwMessage(e) + "\n";
				command = "";
				ISourceLocation location = e.getLocation();
				if (location != null
						&& !location.getURI().getScheme().equals("stdin")) {
					setMarker(e.getMessage(), location.getURI(),
							location.getOffset(), location.getLength());
					error = new CommandExecutionException(content,
							location.getOffset(), location.getLength());
				}
			} catch (Throwable e) {
				Activator.getInstance().logException(e.getMessage(), e);
				content += throwableMessage(e, eval.getStackTrace()) + "\n";
				command = "";
			}
			finally {
				/* References might be <code>null</code> when #terminate() was called beforehand */ 
				try {
					consoleStreamPipe.signalAndWaitForFlush(500); // try to get the most out of the console
					eval.revertToDefaultWriters();
				} catch (NullPointerException e) {
					Activator.getInstance().logException(e.getMessage(), e);
				}
			}
		}
		rm.endJob(true);
		return Status.OK_STATUS;
	}

	public synchronized boolean execute(String cmd) throws CommandExecutionException, TerminationException{
		if(cmd.trim().length() == 0){
			content = ReadEvalPrintDialogMessages.CANCELLED + "\n";
			command = "";
			return true;
		}

		try {
			command += cmd;
			
			if (cmd.startsWith(":test")) {
				// this is very expensive because it triggers all kinds of build actions, so
				// we only do it before running the :test command
				project.getWorkspace().getRoot().deleteMarkers(IRascalResources.ID_RASCAL_MARKER_TYPE_TEST_RESULTS, false, IResource.DEPTH_INFINITE);
			}
			
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
	
	private void setMarker(final String message, final URI location, final int offset, final int length){
		// This code makes sure that if files are read or written during Rascal execution, the build
		// infra-structure of Eclipse is not triggered
		IWorkspaceRunnable action = new IWorkspaceRunnable(){
			public void run(IProgressMonitor monitor) throws CoreException{
				try{
					if (project != null && location.getScheme().equals("project")) {
						lastMarked = project.getFile(location.getPath());
						
						if (!lastMarked.exists()) {
							for (IProject ref : project.getReferencedProjects()) {
								lastMarked = ref.getFile(location.getPath());
							}
						}

						if (lastMarked != null && lastMarked.exists()) {
							IMarker m = lastMarked.createMarker(IRascalResources.ID_RASCAL_MARKER);

							m.setAttribute(IMarker.TRANSIENT, true);
							m.setAttribute(IMarker.CHAR_START, offset);
							m.setAttribute(IMarker.CHAR_END, offset + length);
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
				eval.setTestResultListener(new TestReporter(eval.getResolverRegistry()));
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
		
		content = resultMessage(result) + "\n";
		reportAmbiguities(result);
		command = "";
	}
	
	private void reportAmbiguities(final Result<IValue> result) {
		if (result == null || result.getValue() == null) {
			return;
		}

		if (result.getType().isSubtypeOf(Factory.Tree)) {
			new UIJob("Reporting Ambiguities") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						ReportView part = (ReportView) PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow()
								.getActivePage()
								.showView(ReportView.ID);
						monitor.beginTask("listing ambiguities", 10000);
						part.list(project.getName(), ModuleEnvironment.SHELL_MODULE, (IConstructor) result.getValue(), monitor);
						monitor.done();
					} catch (PartInitException e) {
						RuntimePlugin.getInstance().logException("could not parse module", e);
					}

					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	private void editCommand(final Edit x){
		
		UIJob job = new UIJob("start editor") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					String module = Names.fullName(x.getName());
					URI uri = ResourcesToModules.uriFromModule(eval.getRascalResolver(), module);
					IWorkbench wb = PlatformUI.getWorkbench();
					IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
					final IWorkbenchPage page = win.getActivePage();
					IDE.openEditor(page, uri, UniversalEditor.EDITOR_ID, true);
				} catch (PartInitException e) {
					Activator.log("edit", e);
				} catch (NullPointerException e) {
					// The above code could easily throw null pointer exceptions at every
					// turn, so instead of checking 5 times for null, we catch it here and
					// ignore it.
					eval.getStdErr().print("Could not open " + x.getName());
				}
				
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
	}

	private void clearErrorMarker() {
		if (lastMarked != null) {
			try {
				lastMarked.deleteMarkers(IRascalResources.ID_RASCAL_MARKER, false, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				Activator.getInstance().logException("marker", e);
			}
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

	public AbstractInterpreterEventTrigger getEventTrigger() {
		return eventTrigger;
	}

	public void setEventTrigger(AbstractInterpreterEventTrigger eventTrigger) {
		this.eventTrigger = eventTrigger;
	}
	
}
