/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.jface.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dancingbear.graphbrowser.editor.gef.editparts.NodeEditPart;
import org.dancingbear.graphbrowser.editor.gef.ui.parts.GraphEditor;
import org.dancingbear.graphbrowser.editor.ui.input.GraphEditorInput;
import org.dancingbear.graphbrowser.model.IModelEdge;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.dancingbear.graphbrowser.model.ModelGraphRegister;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * Filter selected nodes with direct connected edges
 * 
 * @author Jeroen van Lieshout
 * @date 10-03-2009
 */
public class ExcludeFilterAction extends Action {

    private IWorkbenchPage page;

    public ExcludeFilterAction(IWorkbenchPage page) {
        this.page = page;
    }

    /**
     * Execute the FilterAction. Opens an new editor with the nodes which are
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

        ArrayList<IModelNode> nodeList = new ArrayList<IModelNode>();

        for (int i = 0; i < selectedItems.size(); i++) {
            if (selectedItems.get(i) instanceof NodeEditPart) { // if selected
                // item is a node, add node to list
                NodeEditPart nodePart = (NodeEditPart) selectedItems.get(i);
                nodeList.add(nodePart.getCastedModel());
            }
        }

        if (nodeList.size() == 0) {
            return; // no nodes, so don't do anything
        }

        // construct new graph to use in new editor
        int graphNumber = 1;
        IModelGraph newGraph = null;
        boolean nameDetermined = false;
        do {
            if (ModelGraphRegister.getInstance().isGraphOpen(
                    "FilteredGraph" + graphNumber) == false) {
                newGraph = ModelGraphRegister.getInstance().getModelGraph(
                        "FilteredGraph" + graphNumber);
                newGraph.setProperty("name", "FilteredGraph" + graphNumber);
                nameDetermined = true;
            } else {
                graphNumber++;
            }
        } while (nameDetermined == false);
        if (newGraph == null) {
            return;
        }

        // Make deep copy of old graph
        Collection<IModelNode> nodes = activeEditor.getGraph().getNodes();
        Collection<IModelEdge> edges = activeEditor.getGraph().getEdges();

        // copy nodes with nonDefaultProperties
        for (IModelNode node : nodes) {
            newGraph.addNode(node.getName(), node.getNonDefaultProperties());
        }

        // copy edges with nonDefaultProperties
        for (IModelEdge edge : edges) {
            newGraph.addEdge(edge.getSource().getName(), edge.getTarget()
                    .getName(), edge.getNonDefaultProperties());
        }

        // remove selected nodes from graph
        for (int i = 0; i < nodeList.size(); i++) {
            String nodeName = nodeList.get(i).getName();
            newGraph.removeNode(nodeName);
        }

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