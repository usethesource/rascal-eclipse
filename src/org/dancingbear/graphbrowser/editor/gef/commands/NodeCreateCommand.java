/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.commands;

import org.dancingbear.graphbrowser.model.IModelGraph;
import org.eclipse.gef.commands.Command;

/**
 * Create new node in graph
 * 
 * @author Michel de Graaf
 * 
 */
public class NodeCreateCommand extends Command {

    private IModelGraph graph;
    private String nodeName;

    /**
     * Add new node to graph
     */
    @Override
    public void execute() {
        graph.addNode(nodeName);
    }

    /**
     * Get graph
     * 
     * @return the graph
     */
    public IModelGraph getGraph() {
        return graph;
    }

    /**
     * Set graph
     * 
     * @param graph the graph to set
     */
    public void setGraph(Object graph) {
        if (graph instanceof IModelGraph) {
            this.graph = (IModelGraph) graph;
        }
    }

    /**
     * Set name of node
     * 
     * @param nodeName the nodeName to set
     */
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Get node name
     * 
     * @return nodeName
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Undo adding new node to graph
     */
    @Override
    public void undo() {
        graph.removeNode(nodeName);
    }

}