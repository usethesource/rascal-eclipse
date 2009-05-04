/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.editpolicies;

import org.dancingbear.graphbrowser.editor.gef.commands.NodeDeleteCommand;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

/**
 * Policy for Node Delete Action
 * 
 * @author Michel de Graaf
 * @author Erik Slagter
 * @author Jeroen van Schagen
 * 
 */
public class NodeDeletePolicy extends ComponentEditPolicy {

    /**
     * Get delete command
     * 
     * @param deleteRequest Request for node delete
     * @return deleteCommand
     */
    @Override
    protected Command createDeleteCommand(GroupRequest deleteRequest) {
        NodeDeleteCommand command = new NodeDeleteCommand();
        if (getHost().getModel() instanceof IModelNode) {
            command.setNode((IModelNode) getHost().getModel());
        }
        return command;
    }

}
