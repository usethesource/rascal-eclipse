package org.meta_environment.rascal.eclipse.console;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dltk.console.IScriptConsoleIO;
import org.eclipse.dltk.console.IScriptConsoleInterpreter;
import org.eclipse.dltk.console.IScriptInterpreter;
import org.eclipse.dltk.console.ScriptConsoleHistory;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.impl.reference.ValueFactory;
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
import org.meta_environment.errors.SummaryAdapter;
import org.meta_environment.rascal.ast.ASTFactory;
import org.meta_environment.rascal.ast.Command;
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
import org.meta_environment.rascal.interpreter.Evaluator;
import org.meta_environment.rascal.interpreter.Names;
import org.meta_environment.rascal.interpreter.control_exceptions.QuitException;
import org.meta_environment.rascal.interpreter.control_exceptions.Throw;
import org.meta_environment.rascal.interpreter.env.ModuleEnvironment;
import org.meta_environment.rascal.interpreter.load.FromResourceLoader;
import org.meta_environment.rascal.interpreter.staticErrors.StaticError;
import org.meta_environment.rascal.parser.ASTBuilder;
import org.meta_environment.rascal.parser.Parser;
import org.meta_environment.uptr.Factory;

public class RascalScriptInterpreter implements IScriptInterpreter {
	private final static ASTFactory factory = new ASTFactory();
	private final ASTBuilder builder = new ASTBuilder(factory);
	private final Parser parser;
	private final static IValueFactory vf = ValueFactory.getInstance();
	private Evaluator eval;
	private final RascalConsole console;
	private String command;
	private String content;
	private int state = IScriptInterpreter.WAIT_NEW_COMMAND;
	private Runnable listener;
	private IFile lastMarked;

	public RascalScriptInterpreter(RascalConsole console) {
		this.console = console;
		this.command = "";
			
		locateRascalParseTable();	
		this.parser = Parser.getInstance();
		this.eval = new Evaluator(vf, factory, new PrintWriter(
				System.err), new ModuleEnvironment("***shell***"));
		
		eval.addModuleLoader(new ProjectModuleLoader());
		eval.addModuleLoader(new FromResourceLoader(RascalScriptInterpreter.class, "org/meta_environment/rascal/eclipse/lib"));
		eval.addClassLoader(getClass().getClassLoader());
		
		loadCommandHistory();
	}

	private void locateRascalParseTable() {
		URL url = FileLocator.find(Platform.getBundle("rascal"), new Path(Parser.PARSETABLE_FILENAME), null);
		try {
			url = FileLocator.resolve(url);
			System.setProperty(Parser.PARSETABLE_PROPERTY, url.getPath());
		} catch (IOException e) {
			Activator.getInstance().logException("internal error", e);
		}
	}
	
	public void exec(String cmd) throws IOException {
		if (cmd.trim().length() == 0) {
			content = "cancelled\n";
			state = IScriptConsoleInterpreter.WAIT_NEW_COMMAND;
			command = "";
			return;
		}

		try {
			command += cmd;
			IConstructor tree = parser.parseFromString(command, "console");

			Type constructor = tree.getConstructorType();

			if (constructor == Factory.ParseTree_Summary) {
				execParseError(tree);
				return;
			}
			else {
				execCommand(tree);
			}
		} 
		catch (StaticError e) {
			content = e.getMessage() + "\n";
			state = IScriptConsoleInterpreter.WAIT_NEW_COMMAND;
			command = "";
			setMarker(e.getMessage(), e.getLocation());
		}
		catch (Throw e) {
			content = e.getMessage() + "\n";
			state = IScriptConsoleInterpreter.WAIT_NEW_COMMAND;
			command = "";
			setMarker(e.getMessage(), e.getLocation());
		}
		catch (QuitException q) {
			clearErrorMarker();
			ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] {console});
		}
		catch (Throwable e) {
			content = "internal exception: " + e.toString() + "\n";
			e.printStackTrace();
			command = "";
			state = IScriptConsoleInterpreter.WAIT_NEW_COMMAND;
		}
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
		Command stat = builder.buildCommand(tree);
		
		clearErrorMarker();

		IValue value = stat.accept(new NullASTVisitor<IValue>() {
			@Override
			public IValue visitCommandStatement(Statement x) {
				return eval.eval(x.getStatement());
			}

			@Override
			public IValue visitCommandDeclaration(Declaration x) {
				return eval.eval(x.getDeclaration());
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
			content = value.toString() + "\n";
		} else {
			content = "ok\n";
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
			state = IScriptInterpreter.WAIT_USER_INPUT;
			content = "";
		}
		else {
			state = IScriptInterpreter.WAIT_NEW_COMMAND;
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
		return content;
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
}
