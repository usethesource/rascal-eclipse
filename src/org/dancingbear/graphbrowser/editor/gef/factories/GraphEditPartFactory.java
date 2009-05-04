/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.factories;

import org.dancingbear.graphbrowser.editor.gef.editparts.EdgeEditPart;
import org.dancingbear.graphbrowser.editor.gef.editparts.GraphEditPart;
import org.dancingbear.graphbrowser.editor.gef.editparts.NodeEditPart;
import org.dancingbear.graphbrowser.editor.gef.editparts.SubgraphEditPart;
import org.dancingbear.graphbrowser.model.IModelEdge;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.dancingbear.graphbrowser.model.IModelSubgraph;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

/**
 * This factory creates EditParts
 * 
 * @author Jeroen van Schagen
 * 
 */
public class GraphEditPartFactory implements EditPartFactory {

    /**
     * Create an editpart
     * 
     * @param context EditPart to create
     * @param model Model to add to EditPart
     */
    public EditPart createEditPart(EditPart context, Object model) {

        EditPart part = null;

        if (model instanceof IModelNode) {
            part = new NodeEditPart();
            part.setModel(model);
        } else if (model instanceof IModelSubgraph) {
            part = new SubgraphEditPart();
            part.setModel(model);
        } else if (model instanceof IModelEdge) {
            part = new EdgeEditPart();
            part.setModel(model);
        } else if (model instanceof IModelGraph) {
            part = new GraphEditPart();
            part.setModel(model);
        }

        return part;

    }

}
