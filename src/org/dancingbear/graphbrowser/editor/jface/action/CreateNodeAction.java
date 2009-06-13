package org.dancingbear.graphbrowser.editor.jface.action;

import javax.swing.JOptionPane;

import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;

public class CreateNodeAction extends Action {

	private IWorkbenchPage page;

	public CreateNodeAction(IWorkbenchPage page) {
		this.page = page;
	}

	/**
	 * Execute the CreateNodeAction. Opens an new editor with the nodes which are
	 * selected. If no nodes are selected nothing is done.
	 */
	public void run() {
		GraphEditor activeEditor;
		if (page.getActiveEditor() instanceof GraphEditor) {
			activeEditor = (GraphEditor) page.getActiveEditor();
		} else {
			return; // do nothing if no active editor could be determined
		}

		String s = (String) JOptionPane.showInputDialog( "Give the name of the node");
		if ((s != null)) {
			activeEditor.getGraph().addNode(s);
			activeEditor.relayout();
		}

	}

}
