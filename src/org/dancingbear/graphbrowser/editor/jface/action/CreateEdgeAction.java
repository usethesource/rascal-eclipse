package org.dancingbear.graphbrowser.editor.jface.action;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JOptionPane;

import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.dancingbear.graphbrowser.model.DefaultEdgeProperties;
import org.eclipse.jface.action.Action;
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
		GraphEditor activeEditor;
		if (page.getActiveEditor() instanceof GraphEditor) {
			activeEditor = (GraphEditor) page.getActiveEditor();
		} else {
			return; // do nothing if no active editor could be determined
		}

		String s1 = (String) JOptionPane.showInputDialog( "Give the name of the source node");
		String s2 = (String) JOptionPane.showInputDialog( "Give the name of the target node");
		if (s1 != null && s2 != null) {
			activeEditor.getGraph().addEdge(s1, s2, DefaultEdgeProperties.getDefaultProperties());
			activeEditor.relayout();
		}

	}

}
