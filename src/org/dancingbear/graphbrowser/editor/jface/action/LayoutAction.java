package org.dancingbear.graphbrowser.editor.jface.action;

import java.util.List;

import org.dancingbear.graphbrowser.editor.gef.editparts.NodeEditPart;
import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.dancingbear.graphbrowser.layout.Layout;
import org.dancingbear.graphbrowser.layout.LayoutSequence;
import org.dancingbear.graphbrowser.layout.fisheye.FisheyeLayout;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.eclipse.draw2d.Animation;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;

public abstract class LayoutAction extends Action {

	protected IWorkbenchPage page;
	private static final int ANIMATION_TIME = 500;

	public LayoutAction(IWorkbenchPage page) {
		this.page = page;
	}

	public void applyLayout(Layout l) {
		if (page.getActiveEditor() instanceof GraphEditor) {
			Animation.markBegin();
			GraphEditor editor = (GraphEditor) page.getActiveEditor();
			editor.getController().applyLayout(l);			
			Animation.run(ANIMATION_TIME);
			editor.getViewer().flush();
		}
	}


}
