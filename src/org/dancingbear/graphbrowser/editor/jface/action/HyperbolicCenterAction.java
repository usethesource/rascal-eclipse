package org.dancingbear.graphbrowser.editor.jface.action;

import java.util.List;

import org.dancingbear.graphbrowser.editor.gef.editparts.NodeEditPart;
import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.dancingbear.graphbrowser.layout.DirectedGraph;
import org.dancingbear.graphbrowser.layout.DirectedGraphLayout;
import org.dancingbear.graphbrowser.layout.DirectedGraphToModelConverter;
import org.dancingbear.graphbrowser.layout.ModelToDirectedGraphConverter;
import org.dancingbear.graphbrowser.layout.Node;
import org.dancingbear.graphbrowser.layout.hypergraph.HyperbolicLayout;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.eclipse.draw2d.Animation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;

public class HyperbolicCenterAction extends Action {

	private static final int ANIMATION_TIME = 500;
	private IWorkbenchPage page;

	public HyperbolicCenterAction(IWorkbenchPage page) {
		this.page = page;
	}


	@Override
	public void run() {
		if (page.getActiveEditor() instanceof GraphEditor) {
			Animation.markBegin();
			GraphEditor editor = (GraphEditor) page.getActiveEditor();

			// get the selected node
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

			IModelGraph graph = editor.getGraph();
			
			final ModelToDirectedGraphConverter modelToGraphConv = new ModelToDirectedGraphConverter();
			final DirectedGraphToModelConverter graphToModelConvert = new DirectedGraphToModelConverter();

			// Convert graph to directed graph
			DirectedGraph directedGraph = modelToGraphConv.convertToGraph(graph.getName());

			// Apply layout
			DirectedGraphLayout layout = new DirectedGraphLayout();
			layout.visit(directedGraph);
			
			// Apply fisheye layout
			//find the selected root using its name
			HyperbolicLayout layout2 = new HyperbolicLayout();
			for (Node n : directedGraph.getNodes()) {
				if (n.getData().equals(node.getName())) {
					layout2.setCenter(n);
				}
			}
			
			layout2.visit(directedGraph);

			// Store graph
			graphToModelConvert.convertToModel(directedGraph, graph.getName());

			Animation.run(ANIMATION_TIME);
			editor.getViewer().flush();
		}
	}

}