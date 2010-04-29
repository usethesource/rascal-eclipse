package org.rascalmpl.eclipse.box;

import static org.rascalmpl.interpreter.result.ResultFactory.makeResult;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.io.PBFReader;
import org.eclipse.imp.pdb.facts.io.PBFWriter;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeStore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.rascalmpl.ast.ASTFactory;
import org.rascalmpl.ast.AbstractAST;
import org.rascalmpl.ast.Command;
import org.rascalmpl.ast.Module;
import org.rascalmpl.interpreter.BoxEvaluator;
import org.rascalmpl.interpreter.CommandEvaluator;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.FromCurrentWorkingDirectoryLoader;
import org.rascalmpl.interpreter.load.FromResourceLoader;
import org.rascalmpl.interpreter.load.ModuleLoader;
import org.rascalmpl.interpreter.result.Result;
import org.rascalmpl.interpreter.staticErrors.SyntaxError;
import org.rascalmpl.library.IO;
import org.rascalmpl.parser.ASTBuilder;
import org.rascalmpl.uri.FileURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.errors.SubjectAdapter;
import org.rascalmpl.values.errors.SummaryAdapter;
import org.rascalmpl.values.uptr.Factory;

public class MakeBox {

	private static final String SHELL_MODULE = "***shell***";

	private final String varName = "boxData";

	class Data extends ByteArrayOutputStream {

		ByteArrayInputStream get() {
			return new ByteArrayInputStream(this.buf);
		}
	}

	IProject project;

	IFile file;

	@SuppressWarnings("unchecked")
	BoxEvaluator eval = new BoxEvaluator();

	private CommandEvaluator commandEvaluator;

	private ModuleLoader loader;
	private GlobalEnvironment heap;
	private ModuleEnvironment root;

	private Data data;

	private TypeStore ts;

	private Type adt;

	private void execute(String s) {
		try {
			IConstructor tree = commandEvaluator.parseCommand(s);
			if (tree.getConstructorType() == Factory.ParseTree_Summary) {
				System.err.println(tree);
				return;
			}
			Command cmd = new ASTBuilder(new ASTFactory()).buildCommand(tree);
			commandEvaluator.eval(cmd);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	void store(IValue v, String varName) {
		Result<IValue> r = makeResult(v.getType(), v, commandEvaluator);
		root.storeVariable(varName, r);
		r.setPublic(true);
	}

	IValue fetch(String varName) {
		Result<IValue> r = root.getVariable(varName);
		return r.getValue();
	}

	private void start() {
		PrintWriter stderr = new PrintWriter(System.err);
		PrintWriter stdout = new PrintWriter(System.out);
		commandEvaluator = new CommandEvaluator(ValueFactoryFactory
				.getValueFactory(), stderr, stdout, root, heap);
		commandEvaluator.addModuleLoader(new FromResourceLoader(
				this.getClass(), "org/rascalmpl/eclipse/library/"));
		commandEvaluator
				.addModuleLoader(new FromCurrentWorkingDirectoryLoader());
		commandEvaluator.addModuleLoader(new FromResourceLoader(getClass(),
				"org/rascalmpl/eclipse/box/"));
		commandEvaluator.addClassLoader(getClass().getClassLoader());
		Object ioInstance = commandEvaluator.getJavaBridge()
				.getJavaClassInstance(IO.class);
		((IO) ioInstance).setOutputStream(new PrintStream(System.out)); // Set
		// output
		// collector.
	}

	private IValue launch(String resultName) {
		// in = new ByteArrayInputStream(byteArray);

		execute("import Box2Text;");
		try {
			IValue v = new PBFReader().read(ValueFactoryFactory
					.getValueFactory(), ts, adt, data.get());
			store(v, varName);
			if (resultName == null) {
				execute("main(" + varName + ");");
				return null;
			}

			execute(resultName + "=toList(" + varName + ");");
			IValue r = fetch(resultName);
			return r;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private IConstructor handleImport(String moduleName) throws IOException {
		IConstructor tree = commandEvaluator.parseCommand("import "
				+ moduleName + ";");
		if (tree.getConstructorType() == Factory.ParseTree_Summary) {
			SubjectAdapter s = new SummaryAdapter(tree).getInitialSubject();
			for (int i = 0; i < s.getEndColumn(); i++) {
				System.err.println(" ");
			}
			System.err.println("^\n");
			System.err.println("parse error at"
					+ (s.getEndLine() != 1 ? (" line" + s.getEndLine())
							: "") + " column " + s.getEndColumn());
		} else {
			Command stat = new ASTBuilder(new ASTFactory())
					.buildCommand(tree);
			if (stat == null) {
				System.err.println("STAT is NULL");
				return null;
			}
			Result<IValue> value = commandEvaluator.eval(stat);

			if (value.getValue() != null) {
				System.err.println(value.getValue());
				return null;
			}
		}
		return tree;
	}

	public IValue run(URI uri, IAction action) {
		loader = new ModuleLoader();
		// System.setProperty("rascal.options.saveBinaries", "false");
		heap = new GlobalEnvironment();
		InputStream inputStream = null;
		try {
			URIResolverRegistry registry = URIResolverRegistry.getInstance();
			FileURIResolver files = new FileURIResolver();
			registry.registerInput(files.scheme(), files);
			inputStream = URIResolverRegistry.getInstance().getInputStream(uri);
			BufferedReader d = new BufferedReader(new InputStreamReader(
					inputStream));
			String q = d.readLine();
			String[] g = q.split("\\s+");
			String moduleName = g[1];
			root = heap.addModule(new ModuleEnvironment(moduleName));
			start();
			handleImport(moduleName);
			ModuleEnvironment env = heap.getModule(moduleName);
			Module module = commandEvaluator.getModuleLoader().loadModule(
					moduleName, null, env);
			ts = eval.getTypeStore();
			adt = BoxEvaluator.getType();
			// System.err.println("Checked");
			IValue v = eval.evalRascalModule(module);
			data = new Data();
			new PBFWriter().write(v, data, ts);
			IValue r = null;
			if (action == null) {
				r = launch("c");
			} else {
				launch(null);
			}
			data.close();
			return r;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (SyntaxError e) {
			return null;
		}

	}
}
