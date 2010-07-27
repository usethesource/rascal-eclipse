package org.rascalmpl.eclipse.editor;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.services.ILanguageActionsContributor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.rascalmpl.checker.StaticChecker;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.ProjectSDFModuleContributor;
import org.rascalmpl.eclipse.console.ProjectURIResolver;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.load.ISdfSearchPathContributor;
import org.rascalmpl.uri.ClassResourceInputStreamResolver;
import org.rascalmpl.uri.IURIInputStreamResolver;
import org.rascalmpl.values.uptr.ParsetreeAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;

public class StaticCheckModelListener implements ILanguageActionsContributor {
	private final MarkerModelListener marker = new MarkerModelListener();
	
	private static HashMap<IParseController, StaticChecker> checkerMap = new HashMap<IParseController, StaticChecker>();
	
	public IConstructor check(IConstructor parseTree, final IParseController parseController, final IProgressMonitor monitor) {
		if (parseTree == null) return null;
						
		monitor.beginTask("Checking Rascal module " + parseController.getPath().toString(), 1);

		try {
			StaticChecker checker = createCheckerIfNeeded(parseController);
			
			if (checker != null) {
				if (!checker.isInitialized())
					initChecker(checker, parseController);
				
				if (checker.isCheckerEnabled()) {
					IConstructor newTree = checker.checkModule((IConstructor) TreeAdapter.getArgs(ParsetreeAdapter.getTop(parseTree)).get(1));
					if (newTree != null) {
						IConstructor treeTop = ParsetreeAdapter.getTop(parseTree);
						IList treeArgs = TreeAdapter.getArgs(treeTop).put(1, newTree);
						IConstructor newTreeTop = treeTop.set("args", treeArgs).setAnnotation("loc", treeTop.getAnnotation("loc"));
						parseTree = parseTree.set("top", newTreeTop);
						marker.update(parseTree, parseController, monitor);
					} else {
						Activator.getInstance().logException("static checker returned null", new RuntimeException());
					}
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

	public boolean isCheckerEnabled(IParseController parseController) {
		StaticChecker checker = createCheckerIfNeeded(parseController);
		return checker.isCheckerEnabled();
	}
	
	private class EnableTypeCheckerMenuItem extends Action {

		private final IParseController parseController;
		
		public EnableTypeCheckerMenuItem(String text, IParseController parseController) {
			super(text, IAction.AS_CHECK_BOX);
		
			this.parseController = parseController;
			
			StaticChecker checker = createCheckerIfNeeded(parseController);
			
			if (checker.isCheckerEnabled())
				this.setChecked(true);
			else
				this.setChecked(false);
		}

		@Override
		public void run() {
			StaticChecker checker = createCheckerIfNeeded(parseController);

			if (checker.isCheckerEnabled()) {
				checker.disableChecker();
				this.setChecked(false);
			} else {
				checker.enableChecker();
				this.setChecked(true);
			}
		}
	}
	
	private class LoadTypeCheckerMenuItem extends Action {

		private final IParseController parseController;
		
		public LoadTypeCheckerMenuItem(String text, IParseController parseController) {
			super(text, IAction.AS_PUSH_BUTTON);
			this.parseController = parseController;
		}

		@Override
		public void run() {
			StaticChecker checker = createCheckerIfNeeded(parseController);
			checker.load();
		}
	}	
	
	public void contributeToEditorMenu(UniversalEditor editor, IMenuManager menuManager) {
		menuManager.add(new EnableTypeCheckerMenuItem("Enable Checker", editor.getParseController()));
		menuManager.add(new LoadTypeCheckerMenuItem("Reload Checker", editor.getParseController()));
	}

	public void contributeToMenuBar(final UniversalEditor editor, IMenuManager menu) {
	}

	public void contributeToStatusLine(UniversalEditor editor, IStatusLineManager statusLineManager) {
	}

	public void contributeToToolBar(UniversalEditor editor, IToolBarManager toolbarManager) {
	}

	protected void copyToConsole(UniversalEditor editor) {
	}	
	
	public int compareTo(IModelListener o) {
		return 0;
	}

	private void initChecker(StaticChecker checker, final IParseController parseController) {
		checker.init();

		if (parseController.getProject() != null) {
			checker.addRascalSearchPath(URI.create("project://" + parseController.getProject().getName() + "/" + IRascalResources.RASCAL_SRC));
		}
		
		ProjectURIResolver resolver = new ProjectURIResolver();
		checker.registerInputResolver(resolver);
		checker.registerOutputResolver(resolver);

		IURIInputStreamResolver library = new ClassResourceInputStreamResolver("rascal-eclipse-library", RascalScriptInterpreter.class);
		checker.registerInputResolver(library);
		
		checker.addRascalSearchPath(URI.create("rascal-eclipse-library:///org/rascalmpl/eclipse/lib"));
		checker.addRascalSearchPath(URI.create("file:///Users/mhills/Projects/rascal/build/rascal/src/org/rascalmpl/library"));
		
		if (parseController.getProject() != null) {
			checker.addSDFResolver(new ProjectSDFModuleContributor(parseController.getProject().getRawProject()));
		}
		
		checker.addSDFResolver(new ISdfSearchPathContributor() {
			public java.util.List<String> contributePaths() {
				java.util.List<String> result = new LinkedList<String>();
				result.add(System.getProperty("user.dir"));
				result.add(Configuration.getSdfLibraryPathProperty());
				return result;
			}
		});		
	}
	
	private StaticChecker createChecker(IParseController parseController) {
		StaticChecker checker = new StaticChecker();
		checkerMap.put(parseController, checker);
		return checker;
	}

	private StaticChecker createCheckerIfNeeded(IParseController parseController) {
		StaticChecker checker = null;
		if (checkerMap.containsKey(parseController)) {
			checker = checkerMap.get(parseController);
		}
		if (checker == null) {
			checker = createChecker(parseController);
		}
		return checker;
	}
}
