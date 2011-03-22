package org.rascalmpl.eclipse.perspective.actions;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.builder.MarkerCreator;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.IProgressService;
import org.rascalmpl.checker.StaticChecker;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.eclipse.editor.MessageProcessor;
import org.rascalmpl.eclipse.editor.ParseController;
import org.rascalmpl.eclipse.uri.BundleURIResolver;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.uri.ClassResourceInputOutput;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.uptr.TreeAdapter;

public class RunStaticChecker implements IEditorActionDelegate {
	private final MessageProcessor marker = new MessageProcessor();
	private static HashMap<ISourceProject, StaticChecker> checkerMap = new HashMap<ISourceProject, StaticChecker>();
	
	private UniversalEditor editor;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof UniversalEditor) {
			this.editor = (UniversalEditor) targetEditor;
		}
		else {
			this.editor = null;
		}
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void run(IAction action) {
		IProject project = editor.getParseController().getProject().getRawProject();
		IFile file = project.getFile(editor.getParseController().getPath());
		final IMessageHandler handler = new MarkerCreator(file);
		
		if (editor != null) {
			WorkspaceModifyOperation wmo = new WorkspaceModifyOperation(ResourcesPlugin.getWorkspace().getRoot()) {
				public void execute(IProgressMonitor monitor) {
					IConstructor toCheck = (IConstructor)editor.getParseController().getCurrentAst();
					IConstructor res = check(toCheck,editor.getParseController(), handler);
					((ParseController) editor.getParseController()).setCurrentAst(res);
				}
			};
			IProgressService ips = PlatformUI.getWorkbench().getProgressService();
			try {
				ips.run(true, true, wmo);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public IConstructor check(IConstructor parseTree, final IParseController parseController, final IMessageHandler handler) {
		if (parseTree == null) return null;
						
		try {
			StaticChecker checker = createCheckerIfNeeded(parseController.getProject());
			
			if (checker != null) {
				IConstructor newTree = checker.checkModule((IConstructor) TreeAdapter.getArgs(parseTree).get(1));
				if (newTree != null) {
					IConstructor treeTop = parseTree;
					IList treeArgs = TreeAdapter.getArgs(treeTop).put(1, newTree);
					IConstructor newTreeTop = treeTop.set("args", treeArgs).setAnnotation("loc", treeTop.getAnnotation("loc"));
					parseTree = newTreeTop;
					handler.clearMessages();
					marker.process(parseTree, handler);
					handler.endMessages();
				}
			} else {
				Activator.getInstance().logException("static checker could not be created", new RuntimeException());
			}
		} catch (Throwable e) {
			Activator.getInstance().logException("static checker failed", e);
		}
		
		return parseTree;
	}	
	
	private void initChecker(StaticChecker checker, final ISourceProject sourceProject) {
		checker.init();

		if (sourceProject != null) {
			try{
				checker.addRascalSearchPath(new URI("project://" + sourceProject.getName() + "/" + IRascalResources.RASCAL_SRC));
			}catch(URISyntaxException usex){
				throw new RuntimeException(usex);
			}
		}
		
		ProjectURIResolver resolver = new ProjectURIResolver();
		URIResolverRegistry resolverRegistry = checker.getResolverRegistry();
		resolverRegistry.registerInput(resolver);
		resolverRegistry.registerOutput(resolver);
		
		ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(resolverRegistry, "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
		resolverRegistry.registerInput(eclipseResolver);
		checker.addRascalSearchPath(URI.create(eclipseResolver.scheme() + ":///"));
		checker.addClassLoader(getClass().getClassLoader());
		
		BundleURIResolver bundleResolver = new BundleURIResolver(resolverRegistry);
		resolverRegistry.registerInput(bundleResolver);
		resolverRegistry.registerOutput(bundleResolver);

		try {
			String rascalPlugin = FileLocator.resolve(Platform.getBundle("rascal").getEntry("/")).getPath();
			String PDBValuesPlugin = FileLocator.resolve(Platform.getBundle("org.eclipse.imp.pdb.values").getEntry("/")).getPath();
			Configuration.setRascalJavaClassPathProperty(rascalPlugin + File.pathSeparator + PDBValuesPlugin + File.pathSeparator + rascalPlugin + File.separator + "src" + File.pathSeparator + rascalPlugin + File.separator + "bin" + File.pathSeparator + PDBValuesPlugin + File.separator + "bin");
		} catch (IOException e) {
			Activator.getInstance().logException("could not create classpath for parser compilation", e);
		}

		checker.enableChecker();
	}
	
	private StaticChecker createChecker(ISourceProject sourceProject) {
		PrintStream consoleStream = RuntimePlugin.getInstance().getConsoleStream();
		StaticChecker checker = new StaticChecker(new PrintWriter(consoleStream), new PrintWriter(consoleStream));
		checkerMap.put(sourceProject, checker);
		initChecker(checker, sourceProject);
		return checker;
	}

	private StaticChecker createCheckerIfNeeded(ISourceProject sourceProject) {
		StaticChecker checker = null;
		if (checkerMap.containsKey(sourceProject)) {
			checker = checkerMap.get(sourceProject);
		}
		if (checker == null) {
			checker = createChecker(sourceProject);
		}
		return checker;
	}

}
