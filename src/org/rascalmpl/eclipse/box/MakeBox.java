package org.rascalmpl.eclipse.box;

import static org.rascalmpl.interpreter.result.ResultFactory.makeResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import org.rascalmpl.ast.Command;
import org.rascalmpl.ast.Module;
import org.rascalmpl.interpreter.BoxEvaluator;
import org.rascalmpl.interpreter.CommandEvaluator;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.FromResourceLoader;
import org.rascalmpl.interpreter.load.ModuleLoader;
import org.rascalmpl.interpreter.result.Result;
import org.rascalmpl.interpreter.staticErrors.SyntaxError;
import org.rascalmpl.library.IO;
import org.rascalmpl.parser.ASTBuilder;
import org.rascalmpl.uri.FileURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.ValueFactoryFactory;
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
		start();
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

	public IValue run(URI uri, IAction action) {
		// System.setProperty("rascal.options.saveBinaries", "true");
		loader = new ModuleLoader();
		// System.setProperty("rascal.options.saveBinaries", "false");
		heap = new GlobalEnvironment();
		root = heap.addModule(new ModuleEnvironment(SHELL_MODULE));
		URIResolverRegistry registry = URIResolverRegistry.getInstance();
		FileURIResolver files = new FileURIResolver();
		registry.registerInput(files.scheme(), files);
		System.err.println("URI:" + uri);
		try {
			IConstructor tree = loader.parseModule(uri, root);
			System.err.println("parsed");
			Module module = new ASTBuilder(new ASTFactory()).buildModule(tree);
			System.err.println("build");
			ts = eval.getTypeStore();
			adt = BoxEvaluator.getType();
			System.err.println("Checked");
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

	// public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	// }
	//
	// public void setActiveEditor(IAction action, IEditorPart targetEditor) {
	// if (targetEditor.getEditorInput() instanceof FileEditorInput) {
	// project = ((FileEditorInput) targetEditor.getEditorInput())
	// .getFile().getProject();
	// }
	// }

}
