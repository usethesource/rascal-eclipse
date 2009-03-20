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

import org.dancingbear.graphbrowser.controller.IGraphEditor;
import org.dancingbear.graphbrowser.editor.draw2d.figure.SubgraphFigure;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.AbstractFigureManipulator;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.FigureManipulatorFactory;
import org.dancingbear.graphbrowser.editor.gef.editpolicies.NodeLayoutPolicy;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.IModelSubgraph;
import org.dancingbear.graphbrowser.model.IPropertyContainer;
import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;

/**
 * EditPart for IModelSubGraph
 * 
 * @author Lars de Ridder
 * @author Jeroen van Schagen
 * @author Taco Witte
 */
public class SubgraphEditPart extends
        AbstractPropertyContainerEditPart<IModelSubgraph> implements
        ActionListener {

    // The width and height defaults should be the default of a node, but is not
    // yet implemented.
    private final int defaultWidth = 90;
    private final int defaultHeight = 40;

    private AbstractFigureManipulator manipulator = FigureManipulatorFactory
            .getDefault().getFigureManipulator("subgraph");

    /**
     * Create edit policies for model
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new NodeLayoutPolicy());
    }

    /**
     * Create the figure
     * 
     * @return subgraphFigure SubGraph as IFigure
     */
    @Override
    protected IFigure createFigure() {
        SubgraphFigure figure = new SubgraphFigure();
        figure.addActionListener(this);
        return figure;
    }

    /**
     * Get children of model
     * 
     * @return children List of children of model
     */
    @Override
    protected List<?> getModelChildren() {

        List<IPropertyContainer> children = new ArrayList<IPropertyContainer>();

        // don't show its children otherwise they will be drawn
        // the children are also in the graph nodelist and that one will draw
        // them
        if (this.isCollapsed()) {
            return children;
        }

        if (getCastedModel() instanceof IModelGraph) {
            children.addAll(getCastedModel().getDirectNodes());
            children.addAll(getCastedModel().getDirectSubgraphs());
            return children;
        }

        return super.getModelChildren();
    }

    /**
     * Refresh visuals
     */
    @Override
    protected void refreshVisuals() {
        // DEBUG
        if (getCastedModel().isCluster()) {
            SubgraphFigure figure = (SubgraphFigure) getFigure();
            Rectangle bounds = getBounds(getCastedModel());

            if (getCastedModel().getPosition() != null) {

                if (isCollapsed()) {
                    bounds.width = defaultWidth;
                    bounds.height = defaultHeight;
                }

                figure.setBounds(bounds);
            }

            figure.setText(getCastedModel().getName());
            figure.setCollapsed(isCollapsed());

        }

        manipulator.manipulateFigure(getCastedModel(), getFigure());

    }

    /**
     * Get bounds of the specified subgraph
     * 
     * @param subgraph Subgraph to get bounds from
     * @return bounds Bounds as rectangle
     */
    private Rectangle getBounds(IModelSubgraph subGraph) {

        int x = (int) subGraph.getPosition().getX();
        int y = (int) subGraph.getPosition().getY();
        int width = Integer.parseInt(subGraph.getProperty("width"));
        int height = Integer.parseInt(subGraph.getProperty("height"));

        return new Rectangle(x, y, width, height);

    }

    /**
     * Let the EditPart know that some property has changed
     * 
     * @param event Event which has been occurred
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(
                IPropertyContainer.CONTAINER_PROPERTY)) {
            refreshVisuals();
        }
    }

    /**
     * Fired when the collapse button of a subgraph is pressed
     */
    public void actionPerformed(ActionEvent event) {
        setCollapsed(!isCollapsed());
        refreshChildren();
    }

    /**
     * Return whether this subgraph is collapsed.
     * 
     * @return whether the subgraph is collapsed
     */
    public boolean isCollapsed() {
        return "true".equals(getCastedModel().getProperty("collapsed"));
    }

    /**
     * Indicate whether this subgraph should be collapsed.
     * 
     * @param collapsed whether the subgraph should be collapsed
     */
    public void setCollapsed(boolean collapsed) {
        String value = collapsed ? "true" : "false";
        getCastedModel().setProperty("collapsed", value);

        relayout();
    }

    /**
     * Perform a relayout action on the GraphEditor
     */
    protected void relayout() {
        IGraphEditor graphEditor = getGraphEditor();
        graphEditor.relayout();
    }
}