/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.commands;

import org.dancingbear.graphbrowser.model.IModelEdge;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.eclipse.gef.commands.Command;

/**
 * Create an edge in the graph
 * 
 * @author Michel de Graaf
 * 
 */
public class EdgeCreateCommand extends Command {

    private IModelNode source, target;
    private IModelEdge edge;

    public EdgeCreateCommand() {
        super();
    }

    /**
     * Add edge
     */
    @Override
    public void execute() {
        edge = target.addIncomingEdge(source);
    }

    /**
     * Get the edge
     * 
     * @return edge
     */
    public IModelEdge getEdge() {
        return edge;
    }

    /**
     * Get source of the edge
     * 
     * @return source
     */
    public IModelNode getSource() {
        return source;
    }

    /**
     * Get target of the edge
     * 
     * @return target
     */
    public IModelNode getTarget() {
        return target;
    }

    /**
     * Set the edge
     * 
     * @param edge Edge to set
     */
    public void setEdge(Object edge) {
        if (edge instanceof IModelEdge) {
            this.edge = (IModelEdge) edge;

            // Update nodes
            source = this.edge.getSource();
            target = this.edge.getTarget();
        }
    }

    /**
     * Set source of edge
     * 
     * @param source Source as object
     */
    public void setSource(Object source) {
        if (source instanceof IModelNode) {
            this.source = (IModelNode) source;
        }
    }

    /**
     * Set target of edge
     * 
     * @param target Target as object
     */
    public void setTarget(Object target) {
        if (target instanceof IModelNode) {
            this.target = (IModelNode) target;
        }
    }

    /**
     * Undo this command
     */
    @Override
    public void undo() {
        target.removeIncomingEdge(edge);
    }

}
