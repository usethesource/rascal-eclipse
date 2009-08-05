package org.dancingbear.graphbrowser.editor.jface.action;

import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.dancingbear.graphbrowser.layout.Layout;
import org.dancingbear.graphbrowser.layout.LayoutSequence;
import org.dancingbear.graphbrowser.layout.dot.DotLayout;
import org.dancingbear.graphbrowser.layout.zest.ZestLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.AbstractLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.GridLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.HorizontalTreeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.SpringLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.VerticalLayoutAlgorithm;

public class ZestLayoutAction extends LayoutAction {

	protected static AbstractLayoutAlgorithm[] algorithms = new AbstractLayoutAlgorithm[]{
		new SpringLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING),
		new TreeLayoutAlgorithm(LayoutStyles.NONE),
		new HorizontalTreeLayoutAlgorithm(LayoutStyles.NONE),
		new RadialLayoutAlgorithm(LayoutStyles.NONE),
		new GridLayoutAlgorithm(LayoutStyles.NONE),
		new HorizontalLayoutAlgorithm(LayoutStyles.NONE),
		new VerticalLayoutAlgorithm(LayoutStyles.NONE)
	};

	protected static String[] algorithmNames = new String[]{
		"Spring",
		"Fade",
		"Tree - V",
		"Tree - H",
		"Radial",
		"Grid",
		"Horizontal",
	"Vertical"};

	public ZestLayoutAction(IWorkbenchPage page) {
		super(page);
	}


	@Override
	public void run() {
		if (page.getActiveEditor() instanceof GraphEditor) {
			//open a dialog frame to select the zest layout
			LabelProvider labelProvider = new LabelProvider() {
				public String getText(Object element) {
					//return only the name of the class
					return element.getClass().getSimpleName();	
				}

			};

			ListSelectionDialog dlg =
				new ListSelectionDialog(
						Display.getCurrent().getActiveShell(),
						algorithms,
						new ArrayContentProvider(),
						labelProvider,
				"Select layout to apply:");
			dlg.setTitle("Apply a Zest layout");
			dlg.open();

			Object[] results = dlg.getResult();
			// We need to re-apply the last layout to distribute 
			// the nodes from the translated directed graph
			//Layout last = editor.getController().getLastLayout();
			Layout last = new DotLayout();
			for (Object e: results) {
				AbstractLayoutAlgorithm algorithm = (AbstractLayoutAlgorithm) e;
				Layout zestLayout = new ZestLayout(algorithm);
				Layout l = new LayoutSequence(last, zestLayout);
				applyLayout(l);			
			}

		}
	}

}
