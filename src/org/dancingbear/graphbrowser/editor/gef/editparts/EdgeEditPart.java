/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.editparts;

import java.beans.PropertyChangeEvent;

import org.dancingbear.graphbrowser.controller.IGraphEditor;
import org.dancingbear.graphbrowser.editor.draw2d.figure.SplineFigure;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.AbstractFigureManipulator;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.FigureManipulatorFactory;
import org.dancingbear.graphbrowser.editor.ui.views.properties.ContainerPropertySource;
import org.dancingbear.graphbrowser.model.IModelEdge;
import org.dancingbear.graphbrowser.model.IPropertyContainer;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * EditPart for IModelEdge
 * 
 * @author Jeroen van Schagen
 * 
 */
public class EdgeEditPart extends AbstractConnectionEditPart implements
        IPropertyContainerEditPart<IModelEdge>, HighlightEditPart {

    private AbstractFigureManipulator manipulator = FigureManipulatorFactory
            .getDefault().getFigureManipulator("edge");

    private ContainerPropertySource propertySource;
    private boolean isHighlighted = false;

    /**
     * Activate EditPart
     */
    @Override
    public void activate() {
        getCastedModel().addPropertyChangeListener(this);
        super.activate();
    }

    /**
     * Create edit policies
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
                new ConnectionEndpointEditPolicy());
    }

    /**
     * Create figure
     * 
     * @return Created figure
     */
    @Override
    protected IFigure createFigure() {
        return new SplineFigure();
    }

    /**
     * Deactivate editpart
     */
    @Override
    public void deactivate() {
        getCastedModel().removePropertyChangeListener(this);
        super.deactivate();
    }

    /**
     * Get model of editpart
     * 
     * @return model of editpart (IModelEdge)
     */
    public IModelEdge getCastedModel() {
        return (IModelEdge) getModel();
    }

    /**
     * Get casted figure of editpart
     * 
     * @return SplineFigure
     */
    public SplineFigure getCastedFigure() {
        return (SplineFigure) getFigure();
    }

    /**
     * Let editpart know that property has been changed
     * 
     * @param event Event which has been raised
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(
                IPropertyContainer.CONTAINER_PROPERTY)) {
            refreshVisuals();
        } else if (event.getPropertyName().equals(IModelEdge.EDGE_SOURCE)) {
            getCastedFigure().resetConnectionRouter();
            refreshVisuals();
        } else if (event.getPropertyName().equals(IModelEdge.EDGE_TARGET)) {
            getCastedFigure().resetConnectionRouter();
            refreshVisuals();
        } else if (event.getPropertyName().equals(IModelEdge.EDGE_SPLINE)) {
            refreshVisuals();
        }

        figureCheckForUIAttributes();
    }

    /**
     * Refresh visuals
     */
    @Override
    protected void refreshVisuals() {
        SplineFigure spFigure = getCastedFigure();
        IModelEdge edge = getCastedModel();
        if (edge != null && spFigure != null) {
            spFigure.setSpline(edge.getSpline());
            manipulator.manipulateFigure(edge, spFigure);
        }

    }

    /**
     * Get adapter
     * 
     * @return adapterObject
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class key) {
        // Return property source of model
        if (key == IPropertySource.class) {
            if (propertySource == null) {
                propertySource = new ContainerPropertySource(getCastedModel());
            }
            return propertySource;
        }
        return super.getAdapter(key);
    }

    /**
     * Returns the highlighted state of this EditPart. This method should only
     * be called internally or by helpers such as EditPolicies.
     * 
     * @return true if highlighted and false if not.
     */
    public boolean getHighlighted() {
        return isHighlighted;
    }

    /**
     * Sets the highlighted state for this EditPart.
     * 
     * @param value the highlighted value
     */
    public void setHighlighted(boolean value) {
        isHighlighted = value;
        figureCheckForUIAttributes();
    }

    /**
     * Set editpart selected state
     */
    @Override
    public void setSelected(int value) {
        super.setSelected(value);
        figureCheckForUIAttributes();
    }

    /**
     * Checks if the figure has to get some extra UI attributes
     */
    private void figureCheckForUIAttributes() {
        refreshVisuals();

        boolean isHighlighted = getHighlighted();
        if (isHighlighted) {
            highlightFigure();
        }

        int selectedValue = getSelected();
        if (selectedValue == EditPart.SELECTED
                || selectedValue == EditPart.SELECTED_PRIMARY) {
            selectFigure();
        }

    }

    /**
     * Set some visual attributes on the figure when it is highlighted.
     */
    private void highlightFigure() {
        this.getFigure().setForegroundColor(ColorConstants.green);
    }

    /**
     * Set some visual attributes on the figure when it is selected.
     */
    private void selectFigure() {
        this.getFigure().setForegroundColor(ColorConstants.red);
    }

    /**
     * Gets the GraphEditor.
     * 
     * @return grapheditor
     */
    public IGraphEditor getGraphEditor() {

        EditDomain domain = this.getViewer().getEditDomain();

        if (domain instanceof DefaultEditDomain) {

            DefaultEditDomain defaultdomain = (DefaultEditDomain) domain;
            IGraphEditor graphEditor = (IGraphEditor) defaultdomain
                    .getEditorPart();

            return graphEditor;
        }

        return null;
    }

}