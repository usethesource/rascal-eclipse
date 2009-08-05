package org.dancingbear.graphbrowser.editor.jface.action;

import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.dancingbear.graphbrowser.layout.Layout;
import org.dancingbear.graphbrowser.layout.LayoutSequence;
import org.dancingbear.graphbrowser.layout.zest.ZestLayout;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;

public class ZestLayoutAction extends LayoutAction {

	public ZestLayoutAction(IWorkbenchPage page) {
		super(page);
	}


	@Override
	public void run() {
		if (page.getActiveEditor() instanceof GraphEditor) {
			final GraphEditor editor = (GraphEditor) page.getActiveEditor();

			//open a dialog frame to select the zest layout
			/**
			InputDialog input = new  InputDialog(Display.getCurrent().getActiveShell(), "Zest layout", "Select a layout", null, null);
			input.open();
			 */

			// We need to re-apply the last layout to distribute 
			// the nodes from the translated directed graph
			Layout last = editor.getController().getLastLayout();
			Layout zestLayout = new ZestLayout(new RadialLayoutAlgorithm());
			Layout l = new LayoutSequence(last, zestLayout);
			applyLayout(l);			

		}
	}

}
