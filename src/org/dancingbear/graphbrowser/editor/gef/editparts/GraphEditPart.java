/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.editparts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.dancingbear.graphbrowser.editor.draw2d.figure.GraphFigure;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.AbstractFigureManipulator;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.FigureManipulatorFactory;
import org.dancingbear.graphbrowser.editor.gef.editpolicies.NodeLayoutPolicy;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.IPropertyContainer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

/**
 * EditPart for IModelGraph
 * 
 * @author Jeroen van Schagen
 * 
 */
public class GraphEditPart extends
        AbstractPropertyContainerEditPart<IModelGraph> {

    private AbstractFigureManipulator manipulator = FigureManipulatorFactory
            .getDefault().getFigureManipulator("graph");

    /**
     * Create edit policies for model
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new NodeLayoutPolicy());
    }

    /**
     * Create figure for this model
     * 
     * @return graphFigure Figure as IFigure
     */
    @Override
    protected IFigure createFigure() {
        return new GraphFigure();
    }

    /**
     * Get children of model
     * 
     * @return children List of children
     */
    @Override
    protected List<?> getModelChildren() {
        List<IPropertyContainer> children = new ArrayList<IPropertyContainer>();
        children.addAll(getCastedModel().getDirectSubgraphs());
        children.addAll(getCastedModel().getDirectNodes());

        return children;
    }

    /**
     * Refresh visuals
     */
    @Override
    protected void refreshVisuals() {
        manipulator.manipulateFigure(getCastedModel(), getFigure());
    }

    /**
     * Notify model that some property has changed
     * 
     * @param event Event wich has been occured
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(
                IPropertyContainer.CONTAINER_PROPERTY)) {
            refreshVisuals();
        } else if (event.getPropertyName().equals(IModelGraph.GRAPH_NODE)) {
            refreshChildren();
        } else if (event.getPropertyName().equals(IModelGraph.GRAPH_EDGE)) {
            refreshChildren();
        } else if (event.getPropertyName().equals(IModelGraph.GRAPH_SUBGRAPH)) {
            refreshChildren();
        }
    }

}