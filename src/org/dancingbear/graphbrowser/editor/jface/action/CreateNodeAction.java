package org.dancingbear.graphbrowser.editor.jface.action;

import javax.swing.JOptionPane;

import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;
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
		final GraphEditor activeEditor;
		if (page.getActiveEditor() instanceof GraphEditor) {
			activeEditor = (GraphEditor) page.getActiveEditor();
		} else {
			return; // do nothing if no active editor could be determined
		}

		InputDialog input = new  InputDialog(Display.getCurrent().getActiveShell(), "Adding Node", "Give the name of the node", null, null);
		input.open();
		final String nodeName = input.getValue();
		if ((nodeName != null)) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					activeEditor.getGraph().addNode(nodeName);
					activeEditor.relayout();
				}
			});
		}

	}

}
