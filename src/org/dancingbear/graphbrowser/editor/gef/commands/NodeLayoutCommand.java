/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.commands;

import org.dancingbear.graphbrowser.model.IModelNode;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

/**
 * Layout node command
 * 
 * @author Erik Slagter
 * 
 */
public class NodeLayoutCommand extends Command {

    private Rectangle currentBounds, oldBounds;
    private IModelNode node;

    /**
     * Change the position of the node
     */
    @Override
    public void execute() {
        node.setProperty("width", "" + currentBounds.width);
        node.setProperty("height", "" + currentBounds.height);
    }

    /**
     * Set the position, and keep the old position
     * 
     * @param bounds Position as rectangle bounds
     */
    public void setBounds(Rectangle bounds) {
        oldBounds = currentBounds;
        currentBounds = bounds;
    }

    /**
     * Set model and automatically update position
     * 
     * @param model the model to set
     */
    public void setModel(Object model) {
        if (model instanceof IModelNode) {
            node = (IModelNode) model;

            // Retrieve current bounds
            int x = (int) node.getPosition().getX();
            int y = (int) node.getPosition().getY();
            int width = Integer.parseInt(node.getProperty("width"));
            int height = Integer.parseInt(node.getProperty("height"));
            currentBounds = new Rectangle(x, y, width, height);
        }
    }

    /**
     * Undo setting position
     */
    @Override
    public void undo() {
        node.setProperty("width", "" + oldBounds.width);
        node.setProperty("height", "" + oldBounds.height);
        node.setPosition(oldBounds.x, oldBounds.y);
    }

    /**
     * Get model of node
     * 
     * @return nodeModel
     */
    public IModelNode getModel() {
        return node;
    }

    /**
     * Get bounds of node
     * 
     * @return bounds Bounds as rectangle
     */
    public Rectangle getBounds() {
        return currentBounds;
    }

}