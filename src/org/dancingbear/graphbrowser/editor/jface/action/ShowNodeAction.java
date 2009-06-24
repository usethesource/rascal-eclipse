package org.dancingbear.graphbrowser.editor.jface.action;

import java.util.Hashtable;
import java.util.List;

import org.dancingbear.graphbrowser.editor.gef.editparts.NodeEditPart;
import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.dancingbear.graphbrowser.editor.ui.input.GraphEditorInput;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.dancingbear.graphbrowser.model.ModelGraphRegister;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.meta_environment.rascal.eclipse.lib.graph.GraphBuilder;

public class ShowNodeAction extends Action {

	private IWorkbenchPage page;

	public ShowNodeAction(IWorkbenchPage page) {
		this.page = page;
	}

	/**
	 * Execute the ShowNodeAction. Opens an new editor with the graph corresponding to the node which is
	 * selected. If no nodes are selected nothing is done.
	 */
	public void run() {
		GraphEditor activeEditor;
		if (page.getActiveEditor() instanceof GraphEditor) {
			activeEditor = (GraphEditor) page.getActiveEditor();
		} else {
			return; // do nothing if no active editor could be determined
		}

		// get all selected nodes
		List<?> selectedItems = activeEditor.getViewer().getSelectedEditParts();
		IModelNode node = null;

		for (int i = 0; i < selectedItems.size(); i++) {
			if (selectedItems.get(i) instanceof NodeEditPart) { // if selected
				// item is a
				// node, add
				// node to list
				NodeEditPart nodePart = (NodeEditPart) selectedItems.get(i);
				node = nodePart.getCastedModel();            }
		}

		if (node ==null) {
			return; // no nodes, so don't do anything
		}

		// construct the new graph to use in the new editor
		int graphNumber = 1;
		IModelGraph newGraph = null;
		boolean nameDetermined = false;
		do {
			if (ModelGraphRegister.getInstance().isGraphOpen(
					"ShowDetail" + graphNumber) == false) {
				newGraph = ModelGraphRegister.getInstance().getModelGraph(
						"ShowDetail" + graphNumber);
				newGraph.setProperty("name", "ShowDetail" + graphNumber);
				nameDetermined = true;
			} else {
				graphNumber++;
			}
		} while (nameDetermined == false);
		if (newGraph == null) {
			return; // It appeared that there is no new graph, so return and do
			// nothing
		}

		// get the node's expression to evaluate
		IValue value = node.getValue();

		//find the current Rascal debug target
		GraphBuilder builder = new GraphBuilder(newGraph);
		builder.computeGraph(value);

		// open graph in new editor
		try {
			activeEditor.getSite().getPage().openEditor(
					new GraphEditorInput(newGraph), GraphEditor.ID, false);
		} catch (PartInitException e) {
			// Exception in opening of new editor
			e.printStackTrace();
		}
	}

}
