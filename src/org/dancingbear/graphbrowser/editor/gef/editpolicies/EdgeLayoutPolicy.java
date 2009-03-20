/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.editpolicies;

import org.dancingbear.graphbrowser.editor.gef.commands.EdgeCreateCommand;
import org.dancingbear.graphbrowser.editor.gef.commands.EdgeReconnectCommand;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

/**
 * Policy for EdgeLayout
 * 
 * @author Erik Slagter
 * @author Joppe Kroon
 * 
 */
public class EdgeLayoutPolicy extends GraphicalNodeEditPolicy {

    /**
     * Get command for Connection Complete
     * 
     * @param request Request of command
     * @return command ConnecionComplete
     */
    @Override
    protected Command getConnectionCompleteCommand(
            CreateConnectionRequest request) {
        Command startCommand = request.getStartCommand();

        EdgeCreateCommand command = (EdgeCreateCommand) startCommand;
        command.setTarget(getHost().getModel());
        return command;
    }

    /**
     * Get command for Connection Create
     * 
     * @param request Request of command
     * @return command Create Connection
     */
    @Override
    protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
        EdgeCreateCommand command = new EdgeCreateCommand();
        command.setSource(getHost().getModel());
        request.setStartCommand(command);
        return command;
    }

    /**
     * Get command for Reconnect Source
     * 
     * @param request Request of command
     * @return command Reconnect Source
     */
    @Override
    protected Command getReconnectSourceCommand(ReconnectRequest request) {
        EdgeReconnectCommand command = new EdgeReconnectCommand();
        command.setEdge(request.getConnectionEditPart().getModel());
        command.setCurrentSource(getHost().getModel());
        return command;
    }

    /**
     * Get command for Reconnect Target
     * 
     * @param request Request of command
     * @return command Reconnect Target
     */
    @Override
    protected Command getReconnectTargetCommand(ReconnectRequest request) {
        EdgeReconnectCommand command = new EdgeReconnectCommand();
        command.setEdge(request.getConnectionEditPart().getModel());
        command.setCurrentTarget(getHost().getModel());
        return command;
    }

}