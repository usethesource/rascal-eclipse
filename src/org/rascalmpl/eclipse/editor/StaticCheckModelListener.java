package org.rascalmpl.eclipse.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.rascalmpl.checker.StaticChecker;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.values.uptr.ParsetreeAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;

public class StaticCheckModelListener implements IModelListener {
	private final StaticChecker checker = StaticChecker.getInstance();
	private final MarkerModelListener marker = new MarkerModelListener();

	public AnalysisRequired getAnalysisRequired() {
		return IModelListener.AnalysisRequired.SYNTACTIC_ANALYSIS;
	}

	public void update(final IParseController parseController,
			final IProgressMonitor monitor) {
		Thread x = new Thread("Rascal Static Checker") {
			@Override
			public void run() {
				monitor.beginTask("Checking Rascal module " + parseController.getPath().toString(), 1);
				IConstructor parseTree = (IConstructor) parseController.getCurrentAst();
				
				if (parseTree == null) {
					return;
				}
				
				try {
					IConstructor newTree = checker.checkModule((IConstructor) TreeAdapter.getArgs(ParsetreeAdapter.getTop(parseTree)).get(1));
					if (newTree != null) {
						IConstructor treeTop = ParsetreeAdapter.getTop(parseTree);
						IList treeArgs = TreeAdapter.getArgs(treeTop).put(1, newTree);
						IConstructor newTreeTop = treeTop.set("args", treeArgs).setAnnotation("loc", treeTop.getAnnotation("loc"));
						parseTree = parseTree.set("top", newTreeTop);
						((ParseController) parseController).setCurrentAst(parseTree);
						marker.update(parseController, monitor);
					}
					else {
						Activator.getInstance().logException("static checker returned null", new RuntimeException());
					}
				}
				catch (Throwable e) {
					Activator.getInstance().logException("static checker failed", e);
				}
				monitor.worked(1);
			}
		};
		x.run();
	}
}
