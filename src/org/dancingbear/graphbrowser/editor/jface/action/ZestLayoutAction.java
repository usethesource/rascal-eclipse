package org.dancingbear.graphbrowser.editor.jface.action;

import java.util.List;

import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.dancingbear.graphbrowser.layout.DirectedGraphToModelConverter;
import org.dancingbear.graphbrowser.layout.Layout;
import org.dancingbear.graphbrowser.layout.ModelToDirectedGraphConverter;
import org.dancingbear.graphbrowser.layout.dot.DirectedGraphLayout;
import org.dancingbear.graphbrowser.layout.model.DirectedGraph;
import org.dancingbear.graphbrowser.layout.model.Edge;
import org.dancingbear.graphbrowser.layout.zest.ZestLayout;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.eclipse.draw2d.Animation;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.zest.layouts.InvalidLayoutConfiguration;
import org.eclipse.zest.layouts.LayoutEntity;
import org.eclipse.zest.layouts.LayoutRelationship;
import org.eclipse.zest.layouts.algorithms.AbstractLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;

public class ZestLayoutAction extends Action {

	private static final int ANIMATION_TIME = 500;

	private IWorkbenchPage page;

	public ZestLayoutAction(IWorkbenchPage page) {
		this.page = page;
	}


	@Override
	public void run() {
		if (page.getActiveEditor() instanceof GraphEditor) {
			Animation.markBegin();

			final GraphEditor editor = (GraphEditor) page.getActiveEditor();
			final IModelGraph graph = editor.getGraph();
			final ModelToDirectedGraphConverter modelToGraphConv = new ModelToDirectedGraphConverter();
			final DirectedGraphToModelConverter graphToModelConvert = new DirectedGraphToModelConverter();

			// Convert graph to directed graph
			final DirectedGraph directedGraph = modelToGraphConv.convertToGraph(graph.getName());

			//open a dialog frame to select the zest layout
			/**
			InputDialog input = new  InputDialog(Display.getCurrent().getActiveShell(), "Zest layout", "Select a layout", null, null);
			input.open();
			 */

			// We need to apply a first layout to distribute the nodes on the canvas
			DirectedGraphLayout layout = new DirectedGraphLayout();
			layout.visit(directedGraph);

			Layout l = new ZestLayout( new RadialLayoutAlgorithm());
			l.visit(directedGraph);

			//it seems that the algorithm only updates the nodes but not the points of the edge
			//we need an extra phase to update the edges
			for (Edge e: directedGraph.getEdges()) {
				PointList pts = new PointList();
				pts.addPoint(e.getSource().getX(), e.getSource().getY());
				pts.addPoint(e.getTarget().getX(),  e.getTarget().getY());
				e.setPoints(pts);
				// no spline defined for this edge so the translation will draw a straight line
				e.setSpline(null);
			}

			// Store graph
			graphToModelConvert.convertToModel(directedGraph, graph.getName());

			Animation.run(ANIMATION_TIME);
			editor.getViewer().flush();

		}

	}

}
