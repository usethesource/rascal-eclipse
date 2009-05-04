/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.editpolicies;

import org.dancingbear.graphbrowser.editor.gef.commands.EdgeDeleteCommand;
import org.dancingbear.graphbrowser.model.IModelEdge;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

/**
 * Policy for Edge Delete Action
 * 
 * @author Erik Slagter
 * 
 */
public class EdgeDeletePolicy extends ConnectionEditPolicy {

    /**
     * Get DeleteCommand
     * 
     * @param arg0 Request of command
     * @return EdgeDeleteCommand
     */
    @Override
    protected Command getDeleteCommand(GroupRequest arg0) {
        EdgeDeleteCommand command = new EdgeDeleteCommand();
        command.setEdge((IModelEdge) getHost().getModel());
        return command;
    }

}
