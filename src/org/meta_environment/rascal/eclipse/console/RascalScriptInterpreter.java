package org.meta_environment.rascal.eclipse.console;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.console.IScriptConsoleIO;
import org.eclipse.dltk.console.IScriptConsoleInterpreter;
import org.eclipse.dltk.console.IScriptInterpreter;
import org.eclipse.dltk.console.ScriptConsoleHistory;
import org.eclipse.dltk.console.ui.ScriptConsolePartitioner;
import org.eclipse.dltk.console.ui.internal.ScriptConsoleViewer;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.part.FileEditorInput;
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
import org.meta_environment.rascal.eclipse.Activator;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory.RascalConsole;
import org.meta_environment.rascal.interpreter.DebuggableEvaluator;
import org.meta_environment.rascal.interpreter.Evaluator;
import org.meta_environment.rascal.interpreter.Names;
import org.meta_environment.rascal.interpreter.control_exceptions.QuitException;
import org.meta_environment.rascal.interpreter.control_exceptions.Throw;
import org.meta_environment.rascal.interpreter.load.FromResourceLoader;
import org.meta_environment.rascal.interpreter.staticErrors.StaticError;
import org.meta_environment.rascal.parser.ASTBuilder;
import org.meta_environment.uptr.Factory;
import org.meta_environment.uptr.TreeAdapter;

public class RascalScriptInterpreter implements IScriptInterpreter{
	private Evaluator eval;
	private final RascalConsole console;
	private String command;
	private String content;
	private int state = IScriptConsoleInterpreter.WAIT_NEW_COMMAND;
	private Runnable listener;
	private IFile lastMarked;

	private final RascalExecutor executor;
	private Thread executorThread;

	public RascalScriptInterpreter(RascalConsole console, Evaluator eval) {
		this.console = console;
		this.command = "";
		this.eval = eval;


		eval.addModuleLoader(new ProjectModuleLoader());
		eval.addModuleLoader(new FromResourceLoader(RascalScriptInterpreter.class, "org/meta_environment/rascal/eclipse/lib"));
		
		eval.addSdfSearchPathContributor(new ProjectSDFModuleContributor());
		
		eval.addClassLoader(getClass().getClassLoader());

		loadCommandHistory();

		executor = new RascalExecutor();
	}

	public void initialize(){
		executorThread = new Thread(executor);
		executorThread.setDaemon(true);
		executorThread.start();
	}

	public Thread getExecutorThread() {
		return executorThread;
	}

	public void exec(String cmd) throws IOException{
		RascalCommand rascalCommand = new RascalCommand(cmd);

		ScriptConsoleHistory history = console.getHistory();
		history.update(cmd);
		history.commit();

		final ScriptConsoleViewer viewer = console.getViewer();
		Control control = viewer.getControl();
		control.getDisplay().syncExec(new Runnable(){
			public void run(){
				viewer.disableProcessing();
				viewer.setEditable(false);
			}
		});

		executor.executeCommand(rascalCommand);
	}

	private static class NotifiableLock{
		private boolean notified = false;

		public void wake(){
			notified = true;
			notify();
		}

		public void block(){
			notified = false;
			do{
				try{
					wait();
				}catch(InterruptedException irex){/* Ignore.*/}
			}while(!notified);
		}
	}

	private class RascalCommand implements Runnable{
		private final String cmd;

		public RascalCommand(String cmd){
			super();

			this.cmd = cmd;
		}

		public void run(){
			try{
				if (cmd.trim().length() == 0) {
					content = "cancelled\n";
					state = IScriptConsoleInterpreter.WAIT_NEW_COMMAND;
					command = "";
					return;
				}

				try {
					command += cmd;

					IConstructor tree = eval.parseCommand(command);

					Type constructor = tree.getConstructorType();

					if (constructor == Factory.ParseTree_Summary) {
						execParseError(tree);
						return;
					}
					execCommand(tree);
				} 
				catch (StaticError e) {
					content = e.getMessage() + "\n";
					state = IScriptConsoleInterpreter.WAIT_NEW_COMMAND;
					command = "";
					setMarker(e.getMessage(), e.getLocation());
					e.printStackTrace();
				}
				catch (Throw e) {
					content = e.getMessage() + "\n";
					String trace = e.getTrace();
					if (trace != null) {
						content += "stacktrace:\n" + trace;
					}
					state = IScriptConsoleInterpreter.WAIT_NEW_COMMAND;
					command = "";
					setMarker(e.getMessage(), e.getLocation());
				}
				catch (QuitException q) {
					content = null;
					clearErrorMarker();
					ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] {console});
					executor.stop();
				}
				catch (Throwable e) {
					content = "internal exception: " + e.toString() + "\n";
					e.printStackTrace();
					command = "";
					state = IScriptConsoleInterpreter.WAIT_NEW_COMMAND;
				}
			}finally{
				updateConsole(console.getViewer(), content);
				content = null;
			}
		}
	}

	public List<Runnable> getRunnables() {
		List<Runnable> l = new ArrayList<Runnable>();
		l.add(executor);
		if (listener!=null) {
			l.add(listener);
		}
		return l;
	}

	private static class RascalExecutor implements Runnable{
		private final LinkedList<RascalCommand> commandQueue;

		private volatile boolean running;

		private final NotifiableLock lock = new NotifiableLock();

		public RascalExecutor(){
			super();

			commandQueue = new LinkedList<RascalCommand>();

			running = true;
		}

		public void executeCommand(RascalCommand rascalCommand){
			synchronized(lock){
				commandQueue.add(rascalCommand);

				lock.wake();
			}
		}

		public void run(){
			while(running){
				RascalCommand command;
				synchronized(lock){
					if(commandQueue.size() == 0 && running){
						lock.block();
					}
					if(!running) break;
					command = commandQueue.remove(0);
				}

				command.run();
			}
		}	

		public void stop(){
			running = false;
		}
	}

	private void updateConsole(final ScriptConsoleViewer viewer, final String text){
		Control control = viewer.getControl();
		if(control == null || text == null) return;

		control.getDisplay().asyncExec(new Runnable(){
			public void run(){
				IDocument document = console.getDocument();
				try{
					document.replace(document.getLength() - 7, 7, text);
					IDocumentPartitioner partitioner = viewer.getDocument().getDocumentPartitioner();
					if(partitioner instanceof ScriptConsolePartitioner){
						ScriptConsolePartitioner scriptConsolePartitioner = (ScriptConsolePartitioner) partitioner;
						scriptConsolePartitioner.clearRanges();
						viewer.getTextWidget().redraw();
					}

					console.getDocumentListener().appendInvitation();
				}catch(BadLocationException e){
					// Won't happen
				}

				viewer.enableProcessing();
				viewer.setEditable(true);
			}
		});
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
				m.setAttribute(IMarker.CHAR_END, loc.getOffset()
						+ loc.getLength());
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
		});


		
		if (value != null) {
			Type type = value.getType();
			if (type.isAbstractDataType() && type.isSubtypeOf(Factory.Tree)) {
				content = "[|" + new TreeAdapter((IConstructor) value).yield() + "|]\n" + 
					type + ": " + value.toString().substring(0, 50) + "...\n";
			}
			else {
				content = value.getType() + ": " + (value != null ? 
						value.toString() : "") + "\n";
			}
		} else {
			content = "ok\n";
		}

		if (eval instanceof DebuggableEvaluator) {
			// need to notify the debugger that the command is finished
			DebuggableEvaluator debugEval = (DebuggableEvaluator) eval;
//			debugEval.getDebugger().stopStepping();
		}
		command = "";
		state = IScriptConsoleInterpreter.WAIT_NEW_COMMAND;
	}

	private void editCommand(Edit x) throws IOException, CoreException {
		String module = Names.name(x.getName());
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
			state = IScriptConsoleInterpreter.WAIT_NEW_COMMAND;//IScriptConsoleInterpreter.WAIT_USER_INPUT;
			content = "";
		}
		else {
			state = IScriptConsoleInterpreter.WAIT_NEW_COMMAND;
			content = "";
			for (int i = 0; i < range.getEndColumn(); i++) {
				content += " ";
			}
			content += "^\nparse error at line " + lastLine + ", column " + range.getEndColumn() + "\n";
			command = "";
		}
	}

	public boolean isValid() {
		return true;
	}

	public String getOutput() {
		return null;
	}

	public int getState() {
		return state;
	}

	@SuppressWarnings("unchecked")
	public List getCompletions(String commandLine, int position)
	throws IOException {
		return Collections.EMPTY_LIST;

	}

	public String getDescription(String commandLine, int position)
	throws IOException {
		return "description???";
	}

	public String[] getNames(String type) throws IOException {
		return null;
	}

	public void close() throws IOException {
	}

	// IScriptConsoleProtocol
	public void consoleConnected(IScriptConsoleIO protocol) {

	}

	public void addCloseOperation(Runnable runnable) {
	}

	public void addInitialListenerOperation(Runnable runnable) {
		if (listener == null) {
			new Thread(runnable).start();
			listener = runnable;
		}
	}

	public InputStream getInitialOutputStream() {
		return new ByteArrayInputStream(new byte[0]);
	}

	@Override
	protected void finalize() throws Throwable {
		clearErrorMarker();
		saveCommandHistory();
	}

	private void loadCommandHistory() {
		try {
			File historyFile = getHistoryFile();
			BufferedReader in = new BufferedReader(new FileReader(historyFile));
			ScriptConsoleHistory history = console.getHistory();

			String command = null;
			while ((command = in.readLine()) != null) {
				history.update(command);
				history.commit();
			}

			in.close();
		} catch (IOException e) {
			Activator.getInstance().logException("history", e);
		}
	}

	private void saveCommandHistory() {
		ScriptConsoleHistory history = console.getHistory();

		OutputStream out = null; 
		try {
			File historyFile = getHistoryFile();

			out = new FileOutputStream(historyFile);
			while (history.prev());

			while (history.next()) {
				String command = history.get();
				out.write(command.getBytes());
				out.write('\n');
			}
		}
		catch (FileNotFoundException e) {
			Activator.getInstance().logException("history", e);
		} 
		catch (IOException e) {
			Activator.getInstance().logException("history", e);
		}
		finally {
			if (out != null) {
				try {
					out.close();
				} 
				catch (IOException e) {
					Activator.getInstance().logException("history", e);
				}
			}
		}
	}

	private File getHistoryFile() throws IOException {
		File home = new File(System.getProperty("user.home"));
		File rascal = new File(home, ".rascal");
		if (!rascal.exists()) {
			rascal.mkdirs();
		}
		File historyFile = new File(rascal, "history");
		if (!historyFile.exists()) {
			historyFile.createNewFile();
		}
		return historyFile;
	}

	private IValue historyCommand() {
		return null;
	}

	public Evaluator getEval() {
		return eval;
	}

	/* construct an Expression AST from a String */ 
	public Expression getExpression(String expression) throws IOException {
		IConstructor tree = eval.parseCommand(expression+";");
		Command c = new ASTBuilder(new ASTFactory()).buildCommand(tree);	
		return c.getStatement().getExpression();
	}

}
