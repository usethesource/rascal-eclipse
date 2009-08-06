package org.dancingbear.graphbrowser.editor.jface.action;

import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.dancingbear.graphbrowser.layout.Layout;
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
