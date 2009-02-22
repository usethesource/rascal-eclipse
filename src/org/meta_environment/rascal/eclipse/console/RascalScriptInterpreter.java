package org.meta_environment.rascal.eclipse.console;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import org.eclipse.dltk.console.IScriptConsoleIO;
import org.eclipse.dltk.console.IScriptConsoleInterpreter;
import org.eclipse.dltk.console.IScriptInterpreter;
import org.eclipse.dltk.console.ScriptConsoleHistory;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceRange;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.impl.reference.ValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.ui.graph.Editor;
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
import org.meta_environment.rascal.eclipse.console.ConsoleFactory.RascalConsole;
import org.meta_environment.rascal.interpreter.Evaluator;
import org.meta_environment.rascal.interpreter.env.ModuleEnvironment;
import org.meta_environment.rascal.parser.ASTBuilder;
import org.meta_environment.rascal.parser.Parser;
import org.meta_environment.uptr.Factory;

public class RascalScriptInterpreter implements IScriptInterpreter {
	private final ASTFactory factory = new ASTFactory();
	private final ASTBuilder builder = new ASTBuilder(factory);
	private final Parser parser = Parser.getInstance();
	private final IValueFactory vf = ValueFactory.getInstance();
	private final Evaluator eval = new Evaluator(vf, factory, new PrintWriter(
			System.err), new ModuleEnvironment("***shell***"));
	private final RascalConsole console;
	private String command;
	private String content;
	private int state = IScriptInterpreter.WAIT_NEW_COMMAND;
	private Runnable listener;

	public RascalScriptInterpreter(RascalConsole console) {
		this.console = console;
		this.command = "";
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
			IConstructor tree = parser.parseFromString(command);

			Type constructor = tree.getConstructorType();

			if (constructor == Factory.ParseTree_Summary) {
				execParseError(tree);
				return;
			}
			else {
				execCommand(tree);
			}
		} catch (Throwable e) {
			content = e.toString() + "\n";
			e.printStackTrace();
			command = "";
			state = IScriptConsoleInterpreter.WAIT_NEW_COMMAND;
		}
	}

	private void execCommand(IConstructor tree) {
		Command stat = builder.buildCommand(tree);

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
				// TODO implement opening an eclipse editor for a file
				// for now, it opens a graph editor for the named
				// variable
				// just a trick to get something working...

				String name = x.getName().toString();
				try {
					IConstructor tree = parser.parseFromString(name + ";");
					Editor.open(builder.buildCommand(tree).accept(this));
				} catch (FactTypeUseException e) {
				} catch (IOException e) {
				}
				return null;
			}

			@Override
			public IValue visitShellCommandHelp(Help x) {
				return null;
			}

			@Override
			public IValue visitShellCommandHistory(History x) {
				ScriptConsoleHistory history = console.getHistory();

				int i = 0;
				while (history.next()) {
					String command = history.get();
					System.err.println(i + ": " + command);
				}

				return null;
			}

			@Override
			public IValue visitShellCommandQuit(Quit x) {
				console.terminate();
				return null;
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

	private void execParseError(IConstructor tree) {
		ISourceRange range = new SummaryAdapter(tree).getInitialErrorRange();
		String[] commandLines = command.split("\n");
		int lastLine = commandLines.length;
		int lastColumn = commandLines[lastLine - 1].length();
		
		if (range.getEndLine() == lastLine && lastColumn <= range.getEndColumn()) { 
			state = IScriptInterpreter.WAIT_USER_INPUT;
			content = "";
		}
		else {
			state = IScriptInterpreter.WAIT_NEW_COMMAND;
			content = "parse error at line " + lastLine + ", column " + range.getEndColumn() + "\n";
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

}
