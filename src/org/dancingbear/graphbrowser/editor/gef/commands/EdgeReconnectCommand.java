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
 * Reconnect an edge to a new source or target in graph
 * 
 * @author Michel de Graaf
 * 
 */
public class EdgeReconnectCommand extends Command {

    private IModelEdge edge;
    private IModelNode oldSource, newSource;
    private IModelNode oldTarget, newTarget;

    public EdgeReconnectCommand() {
        super();
    }

    /**
     * Set an edge
     * 
     * @param edge Edge to set
     */
    public void setEdge(Object edge) {
        if (edge instanceof IModelEdge) {
            this.edge = (IModelEdge) edge;

            // Store node objects
            oldSource = this.edge.getSource();
            oldTarget = this.edge.getTarget();
        }
    }

    /**
     * Set new source of edge
     * 
     * @param source Source to connect to
     */
    public void setCurrentSource(Object source) {
        if (source instanceof IModelNode) {
            newSource = (IModelNode) source;
            newTarget = null;
        }
    }

    /**
     * Set new target of edge
     * 
     * @param target Target to connect to
     */
    public void setCurrentTarget(Object target) {
        if (target instanceof IModelNode) {
            newSource = null;
            newTarget = (IModelNode) target;
        }
    }

    /**
     * Set new source and target of edge
     */
    @Override
    public void execute() {
        if (newSource != null) {
            edge.setSource(newSource);
        } else if (newTarget != null) {
            edge.setTarget(newTarget);
        }
    }

    /**
     * Undo setting new target and source of edge
     */
    @Override
    public void undo() {
        if (newSource != null) {
            edge.setSource(oldSource);
        } else if (newTarget != null) {
            edge.setTarget(oldTarget);
        }
    }

}