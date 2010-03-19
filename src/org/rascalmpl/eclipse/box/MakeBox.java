package org.rascalmpl.eclipse.box;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
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
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.ConsoleFactory;
import org.rascalmpl.interpreter.BoxEvaluator;
import org.rascalmpl.interpreter.RascalShell;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.FromResourceLoader;
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

	private void launch() {
		InputStream input = getInput();
		if (input != null) {
			try {
				new RascalShell(input, new PrintWriter(System.err),
						new PrintWriter(System.out)).run();
				System.err.println("Que le Rascal soit avec vous!");
				System.exit(0);
			} catch (IOException e) {
				System.err.println("unexpected error: " + e.getMessage());
				System.exit(1);
			}
		}
	}

	private void launch(String path) {
		String mode = "run";
		// System.err.println("launch:"+path);
		ILaunchManager launchManager = DebugPlugin.getDefault()
				.getLaunchManager();
		ILaunchConfigurationType type = launchManager
				.getLaunchConfigurationType(IRascalResources.ID_RASCAL_LAUNCH_CONFIGURATION_TYPE);
		try {
			ILaunchConfiguration[] configurations = launchManager
					.getLaunchConfigurations(type);
			for (int i = 0; i < configurations.length; i++) {
				ILaunchConfiguration configuration = configurations[i];
				String attribute = configuration.getAttribute(
						IRascalResources.ATTR_RASCAL_PROGRAM, (String) null);
				// System.err.println("attribute:"+attribute);
				if (path.equals(attribute)) {
					DebugUITools.launch(configuration, mode);
					return;
				}
			}
		} catch (CoreException e) {
			return;
		}

		try {
			// create a new configuration for the rascal file
			ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(
					null, file.getName());
			workingCopy
					.setAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, path);
			ILaunchConfiguration configuration = workingCopy.doSave();
			DebugUITools.launch(configuration, mode);
		} catch (CoreException e1) {
		}
	}

	public void run(IAction action) {
		URI uri = file.getLocationURI();
		// String name = file.getName();
		// name = name.substring(0,name.lastIndexOf('.'));
		final String name = "/ufs/bertl/tst";
		// register some schemes
		// URIResolverRegistry registry = URIResolverRegistry.getInstance();
		// FileURIResolver files = new FileURIResolver();
		// registry.registerInput(files.scheme(), files);
		// uri = FileURIResolver.constructFileURI(uri.getPath());
		ModuleEnvironment root = new ModuleEnvironment("***test***");
		try {
			IConstructor tree = loader.parseModule(uri, root);
			Module module = new ASTBuilder(new ASTFactory()).buildModule(tree);
			TypeStore ts = ((BoxEvaluator) eval).getTypeStore();
			System.err.println("Checked");
			IValue v = eval.evalRascalModule(module);
			// Display display = Display.getCurrent();
			// Shell shell = new Shell (display);
			// shell.open ();
			// FileDialog dialog = new FileDialog (shell, SWT.SAVE);
			// String [] filterExtensions = new String [] {"*.box"};
			// dialog.setFilterExtensions (filterExtensions);
			// dialog.setFileName (name+".box");
			// String file = dialog.open ();
			// dialog.getParent().close();
			// if (file==null) {
			// System.err.println("Canceled");
			// return;
			// }
			// System.out.println ("Save to: " + file);
			// while (!shell.isDisposed ()) {
			// if (!display.readAndDispatch ()) display.sleep ();
			// }
			// display.dispose ();
			PBFWriter.writeValueToFile(v, new File(name + ".box"), ts);
			// // System.err.println("MODULE:" + v);
			// System.err.println("Launch /box/src/Box2Text.rsc");

			// launch("/PrettyPrinting/src/Box2Text.rsc");
			launch();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			System.err
					.println("Selected:" + element + " " + element.getClass());
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

	InputStream getInput() {
		FromResourceLoader fromResourceLoader = new FromResourceLoader(loader
				.getClass(),
				"org/rascalmpl/library/experiments/PrettyPrinting/src");
		final String name = "Box2Text.rsc";
		if (fromResourceLoader.fileExists(name))
			return fromResourceLoader.getInputStream(name);
		else {
			System.err.println("File:" + fromResourceLoader.toString()
					+ " doesn't exists");
			return null;
		}
	}

}
