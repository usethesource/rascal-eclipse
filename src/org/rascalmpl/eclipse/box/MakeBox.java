package org.rascalmpl.eclipse.box;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.io.PBFWriter;
import org.eclipse.imp.pdb.facts.type.TypeStore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.FileEditorInput;
import org.rascalmpl.ast.ASTFactory;
import org.rascalmpl.ast.Command;
import org.rascalmpl.ast.Module;
import org.rascalmpl.eclipse.console.ConsoleFactory;
import org.rascalmpl.interpreter.BoxEvaluator;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.ModuleLoader;
import org.rascalmpl.parser.ASTBuilder;
import org.rascalmpl.uri.FileURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;

public class MakeBox implements IObjectActionDelegate, IActionDelegate2,
		IEditorActionDelegate {

	IProject project;

	IFile file;

	BoxEvaluator eval = new BoxEvaluator();

	private final ModuleLoader loader = new ModuleLoader();

	public void dispose() {
		project = null;
	}

	public void init(IAction action) {
	}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void run(IAction action) {
		IPath path = file.getLocation();
		// System.err.println("path:"+path.toString());
		// register some schemes
		URIResolverRegistry registry = URIResolverRegistry.getInstance();
		FileURIResolver files = new FileURIResolver();
		registry.registerInput(files.scheme(), files);
		URI uri = FileURIResolver.constructFileURI(path.toString());
		// System.err.println("uri:"+uri);
		ModuleEnvironment root = new ModuleEnvironment("***test***");
		try {
			IConstructor tree = loader.parseModule(uri, root);
			Module module = new ASTBuilder(new ASTFactory()).buildModule(tree);
			TypeStore ts = ((BoxEvaluator) eval).getTypeStore();
			IValue v = eval.evalRascalModule(module);
			Display display = Display.getCurrent();
			Shell shell = new Shell (display);
			shell.open ();
			FileDialog dialog = new FileDialog (shell, SWT.SAVE);
			String [] filterExtensions = new String [] {"*.box"};
			dialog.setFilterExtensions (filterExtensions);
			dialog.setFileName ("tst.box");
			String file = dialog.open ();
			System.out.println ("Save to: " + file);
			shell.close();
			while (!shell.isDisposed ()) {
				if (!display.readAndDispatch ()) display.sleep ();
			}	
			display.dispose ();
			PBFWriter.writeValueToFile(v,
					new File(file), ts);
			// System.err.println("MODULE:" + v);
			// System.err.println(stat.getHeader().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			if (element instanceof IProject) {
				project = (IProject) element;
			} else if (element instanceof IFolder) {
				project = ((IFolder) element).getProject();
			} else if (element instanceof IFile) {
				file = ((IFile) element);
			}

		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor.getEditorInput() instanceof FileEditorInput) {
			project = ((FileEditorInput) targetEditor.getEditorInput())
					.getFile().getProject();
		}
	}
}
