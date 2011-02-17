package org.rascalmpl.eclipse.terms;
 
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.parser.IModelListener;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.MarkerModelListener;
import org.rascalmpl.interpreter.result.AbstractFunction;
import org.rascalmpl.interpreter.result.OverloadedFunctionResult;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.TreeAdapter;

/**
 * This class connects the Eclipse IDE with Rascal functions that annotate parse trees.
 * 
 * It makes sure these annotator functions are called after each successful parse.
 * After proper annotations have been added, features such as documentation tooltips
 * and hyperlinking uses to definitions start working.
 * 
 * Note that this class only works for languages that have been registered using the
 * API in SourceEditor.rsc
 */
public class AnnotatorModelListener implements IModelListener {
	private final MarkerModelListener marker = new MarkerModelListener();
	
	public AnalysisRequired getAnalysisRequired() {
		return AnalysisRequired.LEXICAL_ANALYSIS;
	}

	public void update(final IParseController parseController, final IProgressMonitor monitor) {
		final Language lang = parseController.getLanguage();
		final String name = lang.getName();
		
		monitor.beginTask("Annotating " + lang.getName() + " file:" + parseController.getPath().toString(), 1);
		IConstructor parseTree = (IConstructor) parseController.getCurrentAst();

		AbstractFunction func = TermLanguageRegistry.getInstance().getAnnotator(name);

		if (func == null) {
			return;
		}

		if (parseTree == null) {
			return;
		}

		try {
			IConstructor top = parseTree;
			boolean start = false;
			IConstructor tree;
			if (TreeAdapter.getSortName(top).equals("<START>")) {
				tree = (IConstructor) TreeAdapter.getArgs(top).get(1);
				start = true;
			}
			else {
				tree = top;
			}
			
			IConstructor newTree = (IConstructor) func.call(new Type[] {Factory.Tree}, new IValue[] {tree}).getValue();
			
			if (newTree != null) {
				if (start) {
					IList newArgs = TreeAdapter.getArgs(top).put(1, newTree);
					newTree = top.set("args", newArgs).setAnnotation("loc", top.getAnnotation("loc"));
				}
				parseTree = newTree;
				((TermParseController) parseController).setCurrentAst(parseTree);
				marker.update(parseTree, parseController, monitor);
			}
			else {
				Activator.getInstance().logException("annotator returned null", new RuntimeException());
			}
		}
		catch (Throwable e) {
			Activator.getInstance().logException("annotater failed", e);
		}
		monitor.worked(1);
	}

}
