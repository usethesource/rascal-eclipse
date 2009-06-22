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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dancingbear.graphbrowser.editor.draw2d.figure.NodeFigure;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.AbstractFigureManipulator;
import org.dancingbear.graphbrowser.editor.draw2d.figure.manipulator.FigureManipulatorFactory;
import org.dancingbear.graphbrowser.editor.gef.commands.SourceLinkCommand;
import org.dancingbear.graphbrowser.editor.gef.editpolicies.EdgeLayoutPolicy;
import org.dancingbear.graphbrowser.model.IModelEdge;
import org.dancingbear.graphbrowser.model.IModelGraph;
import org.dancingbear.graphbrowser.model.IModelNode;
import org.dancingbear.graphbrowser.model.IPropertyContainer;
import org.eclipse.debug.internal.ui.viewers.ModelNode;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;

/**
 * Edit part implementation for IModelNode
 * 
 * @author Jeroen van Schagen
 * @date 03-03-2009
 */
public class NodeEditPart extends AbstractPropertyContainerEditPart<IModelNode> {

    public static final int selectedNodeBorderWidth = 5;
    public static final int defaultNodeBorderWidth = 1;

    private AbstractFigureManipulator manipulator = FigureManipulatorFactory
            .getDefault().getFigureManipulator("node");

    /**
     * Create node as figure
     */
    @Override
    protected IFigure createFigure() {
        return new NodeFigure();
    }

    /**
     * Create EditPolicies
     */
    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE,
                new EdgeLayoutPolicy());
    }

    /**
     * Get SourceConnections for this node
     * 
     * @return outgoingEdges
     */
    @Override
    protected List<?> getModelSourceConnections() {
        IModelNode model = getCastedModel();
        return model.getOutgoingEdges();
    }
    
    protected void refreshTargetConnections() {
    	int i;
    	ConnectionEditPart editPart;
    	Object model;

    	Map mapModelToEditPart = new HashMap();
    	List connections = getTargetConnections();

    	for (i = 0; i < connections.size(); i++) {
    		editPart = (ConnectionEditPart)connections.get(i);
    		mapModelToEditPart.put(editPart.getModel(), editPart);
    	}

    	List modelObjects = getModelTargetConnections();
    	if (modelObjects == null) modelObjects = new ArrayList();

    	for (i = 0; i < modelObjects.size(); i++) {
    		model = modelObjects.get(i);
    		
    		if (i < connections.size()
    			&& ((EditPart) connections.get(i)).getModel() == model)
    				continue;

    		editPart = (ConnectionEditPart)mapModelToEditPart.get(model);
    		if (editPart != null)
    			reorderTargetConnection(editPart, i);
    		else {
    			editPart = createOrFindConnection(model);
    			addTargetConnection(editPart, i);
    		}
    	}

    	//Remove the remaining Connection EditParts
    	List trash = new ArrayList ();
    	for (; i < connections.size(); i++)
    		trash.add(connections.get(i));
    	for (i = 0; i < trash.size(); i++)
    		removeTargetConnection((ConnectionEditPart)trash.get(i));
    }


    /**
     * Get TargetConnections for this node
     * 
     * @return incomingEdges
     */
    @Override
    protected List<?> getModelTargetConnections() {
        return getCastedModel().getIncomingEdges();
    }

    /**
     * Refresh visuals
     */
    @Override
    protected void refreshVisuals() {
        NodeFigure figure = (NodeFigure) getFigure();
        figure.getShape().setLineWidth(defaultNodeBorderWidth);
        figure.setText(getCastedModel().getName());
        figure.setParentConstraint(getBounds(getCastedModel()));
        manipulator.manipulateFigure(getCastedModel(), figure);
    }
    

    /**
     * Get bounds of the specified node
     * 
     * @param node Node to get bounds from
     * @return bounds Bounds as rectangle
     */
    private Rectangle getBounds(IModelNode node) {

        int x = (int) node.getPosition().getX();
        int y = (int) node.getPosition().getY();
        int width = Integer.parseInt(node.getProperty("width"));
        int height = Integer.parseInt(node.getProperty("height"));

        if (getParent() instanceof AbstractGraphicalEditPart) {
            AbstractGraphicalEditPart parent = (AbstractGraphicalEditPart) getParent();
            Rectangle parentBounds = parent.getFigure().getBounds();

            // Make position relative
            x = x - parentBounds.x;
            y = y - parentBounds.y;
        }

        return new Rectangle(x, y, width, height);

    }

    /**
     * Method to let know some property of node has been changed
     * 
     * @param event Event which has been occurred
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals(
                IPropertyContainer.CONTAINER_PROPERTY)) {
            refreshVisuals();
        } else if (event.getPropertyName().equals(IModelNode.NODE_LAYOUT)) {
            refreshVisuals();
        } else if (event.getPropertyName().equals(IModelNode.NODE_INCOMING)) {
            refreshTargetConnections();
        } else if (event.getPropertyName().equals(IModelNode.NODE_OUTGOING)) {
            refreshSourceConnections();
        }
        figureCheckForUIAttributes();
    }

    /**
     * Perform specific request on node
     * 
     * @param request Request to perform
     */
    @Override
    public void performRequest(Request request) {
        if (request.getType().equals(RequestConstants.REQ_OPEN)) {
            SourceLinkCommand command = new SourceLinkCommand();
            IModelNode selectedNode = (IModelNode) this.getModel();
            command.setSelectedNode(selectedNode);
            command.execute();
        }
    }

    /**
     * Set this specific value as selected
     * 
     * @param value Value to mark as selected
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

        int selectedValue = getSelected();
        if (selectedValue == EditPart.SELECTED
                || selectedValue == EditPart.SELECTED_PRIMARY) {
            selectFigure();
        } else if (selectedValue == EditPart.SELECTED_NONE) {
            deselectFigure();
        }
    }

    /**
     * Set some visual attributes on the figure when it is selected.
     */
    private void selectFigure() {
        NodeFigure nodeFigure = ((NodeFigure) this.getFigure());
        nodeFigure.getShape().setLineWidth(selectedNodeBorderWidth);
        nodeFigure.setForegroundColor(ColorConstants.menuBackgroundSelected);
    }

    /**
     * Restores the visual attributes to the original attributes, when the
     * figure is deselected
     */
    private void deselectFigure() {
        this.refreshVisuals();
    }

}