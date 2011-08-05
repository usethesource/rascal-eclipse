package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.rascalmpl.eclipse.editor.ParseController;
import org.rascalmpl.eclipse.library.util.ValueUI;
import org.rascalmpl.values.ValueFactoryFactory;

public class BrowseTree implements IObjectActionDelegate, IActionDelegate2, IEditorActionDelegate {

	private IEditorPart editor;

	public void dispose() {
		editor = null;
	}

	public void init(IAction action) {}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void run(IAction action) {
		if (editor instanceof UniversalEditor) {
			UniversalEditor ed = (UniversalEditor) editor;
			
			IConstructor tree = (IConstructor) ((ParseController) ed.getParseController()).getCurrentAst();
			
			if (tree != null) {
				IValueFactory valueFactory = ValueFactoryFactory.getValueFactory();
				new ValueUI(valueFactory).tree(tree);
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editor = targetEditor;
	}

}
