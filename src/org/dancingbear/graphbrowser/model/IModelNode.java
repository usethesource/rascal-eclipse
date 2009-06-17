/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.model;

import java.util.List;

import org.eclipse.imp.pdb.facts.IValue;


/**
 * Node model interface.
 * 
 * This model is the node of an an (un)directed graph. The name of the node is
 * the unique identifier, however, a seperate id is generated for internal use.
 * 
 * @author Nickolas Heirbaut
 * @author Jeroen Van Schagen
 */
public interface IModelNode extends IPropertyContainer, IPropertyPublisher {

    String NODE_LAYOUT = "NodeLayout";
    String NODE_INCOMING = "NodeIncoming";
    String NODE_OUTGOING = "NodeOutgoing";

    /**
     * Add an incoming edge to the node. The model will add an edge to the graph
     * with source equals the passed in argument and the target equals this
     * node.
     * 
     * @param sourceNode The source node of the edge.
     * @return IModelEdge The added Incoming edge
     */
    IModelEdge addIncomingEdge(IModelNode sourceNode);

    /**
     * Add an incoming edge to the node. The model will add an edge to the graph
     * with source equals the passed in argument and the target equals this
     * node.
     * 
     * @param sourceNode The unique name of the source node of the edge
     * @return The added incoming edge
     */
    IModelEdge addIncomingEdge(String sourceNode);

    /**
     * Removes the edge of the node.
     * 
     * @param edge The edge to be removed
     * @return True if the edge is removed, false otherwise.
     */
    boolean removeIncomingEdge(IModelEdge edge);

    /**
     * Removes the edge of the node.
     * 
     * @param edgeId The id of the edge
     * @return True if the edge is removed, false otherwise.
     */
    boolean removeIncomingEdge(Integer edgeId);

    /**
     * Get the incoming edge of the node.
     * 
     * @param edgeId The id of the edge
     * @return The outgoing edge
     */
    IModelEdge getIncomingEdge(Integer edgeId);

    /**
     * Get all incoming edges of the node. The incoming edges are the edges
     * where the target node equals this node.
     * 
     * @return The incoming edges
     */
    List<IModelEdge> getIncomingEdges();

    /**
     * Add an outgoing edge to the node. The model will add an edge to the graph
     * with target equals the passed in argument and the source equals this
     * node.
     * 
     * @param sourceNode The target node of the edge.
     * @return The added outgoing edge
     */
    IModelEdge addOutgoingEdge(IModelNode targetNode);

    /**
     * Add an outgoing edge to the node. The model will add an edge to the graph
     * with target equals the passed in argument and the source equals this
     * node.
     * 
     * @param targetNode The unique name of the target node
     * @return The added outgoing edge
     */
    IModelEdge addOutgoingEdge(String targetNode);

    /**
     * Removes the edge of the node.
     * 
     * @param edge The edge to be removed
     * @return True if the edge is removed, false otherwise.
     */
    boolean removeOutgoingEdge(IModelEdge edge);

    /**
     * Removes the edge of the node.
     * 
     * @param edge The id of the edge to be removed
     * @return True if the edge is removed, false otherwise.
     */
    boolean removeOutgoingEdge(Integer edgeId);

    /**
     * Get the outgoing edge of the node.
     * 
     * @param edgeId The id of the edge
     * @return The outgoing edge
     */
    IModelEdge getOutgoingEdge(Integer edgeId);

    /**
     * Get the outgoing edges of the node.
     * 
     * @return List of outgoing edges
     */
    List<IModelEdge> getOutgoingEdges();

    /**
     * Get the unique name of the node
     * 
     * @return String name of Node
     */
    String getName();
    
    /**
     * Get the value of the node
     * 
     * @return IValue value of Node
     */
    IValue getValue();

    /**
     * Get the id of the node
     * 
     * @return Integer Id of the node
     */
    Integer getId();

    /**
     * Get the root graph of the node
     * 
     * @return IModelGraph Graph
     */
    IModelGraph getParentGraph();

    /**
     * Get the position of the node
     * 
     * @return Position Position of the node
     */
    Position getPosition();

    /**
     * Set the position of the node
     * 
     * @param x
     * @param y
     */
    void setPosition(double x, double y);

    /**
     * Set the position of the node
     * 
     * @param position
     */
    void setPosition(Position position);

    /**
     * Get the parent graph of the node
     * 
     * @return The parent graph of the node
     */
    IModelGraph getParent();

    /**
     * Set the parent of the node
     * 
     * @param parent The parent of the node
     */
    void setParent(IModelGraph parent);

}