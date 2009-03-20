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
 * Remove an edge in the graph
 * 
 * @author Michel de Graaf
 * @author Erik Slagter
 * 
 */
public class EdgeDeleteCommand extends Command {

    private IModelEdge edge;
    private IModelNode source, target;

    /**
     * Remove the defined edge
     */
    @Override
    public void execute() {
        source.removeOutgoingEdge(edge);
        target.removeIncomingEdge(edge);
    }

    /**
     * Set the edge
     * 
     * @param edge The edge to set
     */
    public void setEdge(IModelEdge edge) {
        this.edge = edge;
        source = edge.getSource();
        target = edge.getTarget();
    }

    /**
     * Add the edge that has just been deleted
     */
    @Override
    public void undo() {
        source.addOutgoingEdge(target);
        target.addIncomingEdge(source);
    }

}