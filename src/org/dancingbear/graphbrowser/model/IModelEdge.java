/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.model;

/**
 * Edge model interface.
 * 
 * This model is the edge of an (un)directed graph. The edge consists out of a
 * source and target node.
 * 
 * @author Nickolas Heirbaut
 * @author Jeroen Van Lieshout
 */
public interface IModelEdge extends IPropertyContainer, IPropertyPublisher {

    String EDGE_SOURCE = "EdgeSource";
    String EDGE_TARGET = "EdgeTarget";
    String EDGE_SPLINE = "EdgeSpline";

    /**
     * Get the root graph of the edge
     * 
     * @return IModelGraph The root graph of the edge
     */
    IModelGraph getGraph();

    /**
     * Get the id of the edge
     * 
     * @return Id of the edge
     */
    Integer getId();

    /**
     * Get the source node of the edge
     * 
     * @return The source node
     */
    IModelNode getSource();

    /**
     * Set source node of the edge
     * 
     * @param source The source node
     * @return True if the node is set, false otherwise
     */
    boolean setSource(IModelNode source);

    /**
     * Set source node of the edge
     * 
     * @param name The unique name of the node
     * @return True if the node is set, false otherwise
     */
    boolean setSource(String name);

    /**
     * Get target
     * 
     * @return The target node
     */
    IModelNode getTarget();

    /**
     * Set target node
     * 
     * @param target The target node
     * @return True if the node is set, false otherwise
     */
    boolean setTarget(IModelNode target);

    /**
     * Set target node
     * 
     * @param name The name of the node
     * @return True if the node is set, false otherwise
     */
    boolean setTarget(String name);

    /**
     * Get the spline of the edge
     * 
     * Used to draw the line of the edge
     * 
     * @see "http://en.wikipedia.org/wiki/Spline_(mathematics)"
     * 
     * @return The spline
     */
    Spline getSpline();

    /**
     * Set spline
     * 
     * Used to draw the line of the edge
     * 
     * @see "http://en.wikipedia.org/wiki/Spline_(mathematics)"
     * 
     * @param spline The spline of the edge
     * @return True if the spline is set, false otherwise
     */
    boolean setSpline(Spline spline);

}