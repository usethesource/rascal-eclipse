package org.rascalmpl.eclipse.perspective.actions.highlight;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.ParseController;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.perspective.actions.AbstractEditorAction;
import org.rascalmpl.interpreter.Evaluator;

public class HighlightAction extends AbstractEditorAction {

	private String module;
	private String function;
	private String ext;

	public HighlightAction(UniversalEditor editor, String label, String module, String function, String ext) {
		super(editor, label);
		this.module = module;
		this.function = function;
		this.ext = ext;
	}
	
	@Override
	public void run() {
		IConstructor tree = (IConstructor) ((ParseController) editor
				.getParseController()).getCurrentAst();
		if (tree != null) {
			Evaluator eval = ProjectEvaluatorFactory.getInstance()
					.getEvaluator(project);
			eval.doImport(null, "lang::box::util::Highlight");
			eval.doImport(null, module);
			IList hl = (IList) eval.call("highlight", tree);
			IString s = (IString) eval.call(function, hl);

			IWorkbenchPage page = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			try {
				page.openEditor(new StringInput(new StringStorage(editor, project, s.getValue(), ext)),
						"org.eclipse.ui.DefaultTextEditor");
			} catch (PartInitException e) {
				Activator.getInstance().logException("could not open editor for html", e);
			}
		}

	}

}
