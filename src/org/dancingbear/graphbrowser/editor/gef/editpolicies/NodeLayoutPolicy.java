/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.editpolicies;

import org.dancingbear.graphbrowser.editor.gef.commands.NodeLayoutCommand;
import org.dancingbear.graphbrowser.editor.gef.editparts.GraphEditPart;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

/**
 * Policy for Node Layout
 * 
 * @author Erik Slagter
 * @author Jeroen van Schagen
 * 
 */
public class NodeLayoutPolicy extends XYLayoutEditPolicy {

    /**
     * Get ChangeConstraintCommand
     * 
     * @param child EditPart to perform command on
     * @param constraint Constraint to perform
     * @return ChangeConstraintCommand
     */
    @Override
    protected Command createChangeConstraintCommand(EditPart child,
            Object constraint) {
        if (constraint instanceof Rectangle) {
            NodeLayoutCommand command = new NodeLayoutCommand();
            command.setModel(child.getModel());
            command.setBounds((Rectangle) constraint);
            return command;
        }

        return null;
    }

    /**
     * Get create command
     * 
     * @return null TODO: Implement this when factory is implemented
     */
    @Override
    protected Command getCreateCommand(CreateRequest request) {
        if (request == null) {
            return null;
        }

        if (request.getType() == REQ_CREATE
                && getHost() instanceof GraphEditPart) {
            // TODO: When creation factory is implemented
        }

        return null;
    }

}