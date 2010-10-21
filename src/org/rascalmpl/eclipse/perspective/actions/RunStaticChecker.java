package org.rascalmpl.eclipse.perspective.actions;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.rascalmpl.checker.StaticChecker;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.ProjectSDFModuleContributor;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.eclipse.editor.MarkerModelListener;
import org.rascalmpl.eclipse.editor.ParseController;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.load.ISdfSearchPathContributor;
import org.rascalmpl.uri.ClassResourceInputOutput;
import org.rascalmpl.uri.IURIInputStreamResolver;
import org.rascalmpl.values.uptr.ParsetreeAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;

public class RunStaticChecker implements IEditorActionDelegate {
	private final MarkerModelListener marker = new MarkerModelListener();
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
		if (editor != null) {
			try {
				IRunnableWithProgress rup = new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) {
						IConstructor toCheck = (IConstructor)editor.getParseController().getCurrentAst();
						IConstructor res = check(toCheck,editor.getParseController(), monitor);
						((ParseController) editor.getParseController()).setCurrentAst(res);
					}
				};
				IWorkbench wb = PlatformUI.getWorkbench();
				IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				Shell shell = win != null ? win.getShell() : null;
				new ProgressMonitorDialog(shell).run(true, true, rup);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
	}

	public IConstructor check(IConstructor parseTree, final IParseController parseController, final IProgressMonitor monitor) {
		if (parseTree == null) return null;
						
		monitor.beginTask("Checking Rascal module " + parseController.getPath().toString(), 1);
		try {
			StaticChecker checker = createCheckerIfNeeded(parseController.getProject());
			
			if (checker != null) {
				IConstructor newTree = checker.checkModule((IConstructor) TreeAdapter.getArgs(ParsetreeAdapter.getTop(parseTree)).get(1));
				if (newTree != null) {
					IConstructor treeTop = ParsetreeAdapter.getTop(parseTree);
					IList treeArgs = TreeAdapter.getArgs(treeTop).put(1, newTree);
					IConstructor newTreeTop = treeTop.set("args", treeArgs).setAnnotation("loc", treeTop.getAnnotation("loc"));
					parseTree = parseTree.set("top", newTreeTop);
					marker.update(parseTree, parseController, monitor);
				}
			} else {
				Activator.getInstance().logException("static checker could not be created", new RuntimeException());
			}
		} catch (Throwable e) {
			Activator.getInstance().logException("static checker failed", e);
		} finally {
			monitor.worked(1);
		}
		
		return parseTree;
	}	
	
	private void initChecker(StaticChecker checker, final ISourceProject sourceProject) {
		checker.init();

		if (sourceProject != null) {
			checker.addRascalSearchPath(URI.create("project://" + sourceProject.getName() + "/" + IRascalResources.RASCAL_SRC));
		}
		
		ProjectURIResolver resolver = new ProjectURIResolver();
		checker.registerInputResolver(resolver);
		checker.registerOutputResolver(resolver);

//		IURIInputStreamResolver library = new ClassResourceInputOutput("rascal-eclipse-library", RascalScriptInterpreter.class);
//		checker.registerInputResolver(library);
		
		checker.addRascalSearchPath(URI.create("rascal-eclipse-library:///org/rascalmpl/eclipse/lib"));
		checker.addRascalSearchPath(URI.create("file:///Users/mhills/Projects/rascal/build/rascal/src/org/rascalmpl/library"));
		
		checker.enableChecker();
	}
	
	private StaticChecker createChecker(ISourceProject sourceProject) {
		StaticChecker checker = new StaticChecker();
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
