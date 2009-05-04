/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.commands;

import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.eclipse.gef.commands.Command;

/**
 * Command to delete node
 * 
 * @author Michel de Graaf
 * 
 */
public class NodeDeleteCommand extends Command {

    private IModelNode node;

    /**
     * Delete node from graph
     */
    @Override
    public void execute() {
        node.getParentGraph().removeNode(node);
    }

    /**
     * Get node
     * 
     * @return the node
     */
    public IModelNode getNode() {
        return node;
    }

    /**
     * Set node
     * 
     * @param node the node to set
     */
    public void setNode(IModelNode node) {
        this.node = node;
    }

    /**
     * Undo deleting node
     */
    @Override
    public void undo() {
        node.getParentGraph().addNode(node.getName());

        // TODO: Restore properties and edges

        /*
         * FIXME joppe says: I have moved the addNode call to before the adding
         * of the edges to prevent exceptions, however this is not enough to
         * reconnect the node.
         * 
         * 1. A node needs to exist before an edge can be connected to it
         * obviously!
         * 
         * 2. Edges point to a nodeId, which is not the name but a unique int
         * for every created node!
         * 
         * 3. Edges associated to a node are removed from the graph when the
         * node is removed. (missed that one at first, thanks!)
         * 
         * 4. A node is also removed from subgraphs it belongs to, this will
         * need to be reinstated also.
         * 
         * 5. The position of a node could easily be restored also.
         * 
         * 6. The properties of a node should also be passed in the addNode
         * call.
         * 
         * Conclusion: to undo you will need to store the source nodes for the
         * incoming edges and target nodes for outgoing edges along with the
         * removed node, as well as a list of subgraphs the node belonged to.
         * 
         * This is a seriously pain in the ass function to undo, but not
         * impossible.
         * 
         * Happy coding, Joppe
         */
    }

    /**
     * Get graph
     * 
     * @return graph
     */
    public IModelGraph getGraph() {
        return node.getParentGraph();
    }
}
