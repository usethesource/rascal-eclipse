package org.eclipse.imp.pdb.ui.tree;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

public class ExpandAll implements IEditorActionDelegate {
	private IEditorPart editor;

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		this.editor = targetEditor;
	}

	public void run(IAction action) {
		if (editor instanceof Editor) {
			((Editor) editor).getViewer().expandAll();
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
}
