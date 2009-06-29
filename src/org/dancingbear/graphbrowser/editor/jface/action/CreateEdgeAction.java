package org.dancingbear.graphbrowser.editor.jface.action;

import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.dancingbear.graphbrowser.model.DefaultEdgeProperties;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;

public class CreateEdgeAction extends Action {

	private IWorkbenchPage page;

	public CreateEdgeAction(IWorkbenchPage page) {
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
		InputDialog input = new  InputDialog(Display.getCurrent().getActiveShell(), "Adding Edge", "Give the name of the source node", null, null);
		input.open();
		final String sourceName = input.getValue();
		input = new  InputDialog(Display.getCurrent().getActiveShell(), "Adding Edge", "Give the name of the target node", null, null);
		input.open();
		final String targetName = input.getValue();

		if (sourceName != null && targetName != null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					activeEditor.getGraph().addEdge(sourceName, targetName, DefaultEdgeProperties.getDefaultProperties());
					activeEditor.relayout();
				}
			});
		}

	}

}
