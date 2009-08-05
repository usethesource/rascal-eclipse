package org.dancingbear.graphbrowser.editor.jface.action;

import java.util.List;

import org.dancingbear.graphbrowser.editor.gef.editparts.NodeEditPart;
import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.dancingbear.graphbrowser.layout.Layout;
import org.dancingbear.graphbrowser.layout.LayoutSequence;
import org.dancingbear.graphbrowser.layout.dot.DotLayout;
import org.dancingbear.graphbrowser.layout.fisheye.FisheyeLayout;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.eclipse.ui.IWorkbenchPage;

public class FisheyeAction extends LayoutAction {

	public FisheyeAction(IWorkbenchPage page) {
		super(page);
	}

	@Override
	public void run() {
		if (page.getActiveEditor() instanceof GraphEditor) {
			GraphEditor editor = (GraphEditor) page.getActiveEditor();

			// get the selected node as the origin of the hyperbolic plan
			List<?> selectedItems = editor.getViewer().getSelectedEditParts();
			IModelNode node = null;

			for (int i = 0; i < selectedItems.size(); i++) {
				if (selectedItems.get(i) instanceof NodeEditPart) { 
					NodeEditPart nodePart = (NodeEditPart) selectedItems.get(i);
					node = nodePart.getCastedModel();            }
			}

			if (node ==null) {
				return; // no nodes, so don't do anything
			}

			// We need to re-apply the last layout to distribute 
			// the nodes from the translated directed graph
			//Layout last = editor.getController().getLastLayout();
			Layout last = new DotLayout();
			Layout fisheye = new FisheyeLayout(node.getId());
			Layout l = new LayoutSequence(last, fisheye);

			applyLayout(l);			
		}
	}

}