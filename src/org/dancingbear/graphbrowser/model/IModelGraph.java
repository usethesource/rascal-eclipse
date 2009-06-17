/*******************************************************************************
 * Copyright 2009, University of Amsterdam, Amsterdam, The Netherlands
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.dancingbear.graphbrowser.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.imp.pdb.facts.IValue;

/**
 * Graph model interface.
 * 
 * This model is the root graph of an (un)directed graph. The graph can consists
 * out of nodes, edges and subgraphs.
 * 
 * @author Nickolas Heirbaut
 * @author Jeroen Van Lieshout
 */
public interface IModelGraph extends IPropertyContainer, IPropertyPublisher {

    String GRAPH_NODE = "GraphNode";
    String GRAPH_EDGE = "GraphEdge";
    String GRAPH_SUBGRAPH = "GraphSubgraph";

    /**
     * Add a node to the graph.
     * 
     * If the node already exists in the graph, only the properties are updated
     * (if applicable)
     * 
     * @param name The unique name of the node
     * @return The node
     */
    IModelNode addNode(String name);

    /**
     * Add a node to the graph with properties.
     * 
     * If the node already exists in the graph, only the properties are updated
     * (if applicable)
     * 
     * @param name The unique name of the node
     * @param properties The properties of the node
     * @return The node
     */
    IModelNode addNode(String name, Map<String, String> properties);

    /**
     * Add a node to the graph with properties.
     * 
     * If the node already exists in the graph, only the properties are updated
     * (if applicable)
     * 
     * @param parent The parent of the node
     * @param name The unique name of the node
     * @param properties The properties of the node
     * @return The node
     */
    IModelNode addNode(IModelGraph parent, String name,
            Map<String, String> properties);

    /**
     * Removes a node from the graph and any subgraphs if applicable. Edges
     * pointing from or to this node are removed as well.
     * 
     * @param node The unique name of the node
     * @return True if the node is removed, false otherwise
     */
    boolean removeNode(IModelNode node);

    /**
     * Removes a node from the graph and any subgraphs if applicable. Edges
     * pointing from or to this node are removed as well.
     * 
     * @param name The unique name of the node
     * @return True if the node is removed, false otherwise
     */
    boolean removeNode(String name);

    /**
     * Add an edge to the graph
     * 
     * The model allows multiple edges with the same source and target to be
     * added. Each time, a new edge is created.
     * 
     * @param source The source node for the edge
     * @param target The target node for the edge
     * @return The edge
     * @throws NoSuchElementException If source or target node does not exist
     */
    IModelEdge addEdge(IModelNode source, IModelNode target)
            throws NoSuchElementException;

    /**
     * Add an edge to the graph with properties
     * 
     * The model allows multiple edges with the same source and target to be
     * added. Each time, a new edge is created.
     * 
     * @param source The source node for the edge
     * @param target The target node for the edge
     * @param properties The properties of the edges
     * @return The edge
     * @throws NoSuchElementException If source or target node does not exist
     */
    IModelEdge addEdge(IModelNode source, IModelNode target,
            Map<String, String> properties) throws NoSuchElementException;

    /**
     * Add an edge to the graph
     * 
     * The model allows multiple edges with the same source and target to be
     * added. Each time, a new edge is created.
     * 
     * @param sourceName The unique name of the sourcenode for the edge.
     * @param targetName The unique name of the sourcenode for the edge.
     * @return The edge
     * @throws NoSuchElementException If source or target node does not exist
     */
    IModelEdge addEdge(String sourceName, String targetName)
            throws NoSuchElementException;

    /**
     * Add an edge to the graph with properties
     * 
     * The model allows multiple edges with the same source and target to be
     * added. Each time, a new edge is created.
     * 
     * @param sourceName The unique name of the sourcenode for the edge.
     * @param targetName The unique name of the targetnode for the edge.
     * @param properties The properties of the edges
     * @return The edge
     * @throws NoSuchElementException if source or target node does not exist
     */
    IModelEdge addEdge(String sourceName, String targetName,
            Map<String, String> properties) throws NoSuchElementException;

    /**
     * Removes the edge from the graph
     * 
     * @param edge The edge to be removed
     * @return True if the edge is removed, false otherwise
     */
    boolean removeEdge(IModelEdge edge);

    /**
     * Removes the edge from the graph
     * 
     * @param edgeId The id of the edge
     * @return True if the edge is removed, false otherwise
     */
    boolean removeEdge(Integer edgeId);

    /**
     * Add a subgraph to the <b>parent</b> graph with properties.
     * 
     * If the subgraph already exists in the graph, only the properties are
     * updated (if applicable)
     * 
     * @param parent The parent of the subgraph
     * @param name The unique name of the subgraph
     * @param properties The properties of the subgraph
     * @return The subgraph
     */
    IModelSubgraph addSubgraph(IModelGraph parent, String name,
            Map<String, String> properties);

    /**
     * Add subgraph to the graph
     * 
     * If the subgraph already exists in the graph, only the properties are
     * updated (if applicable)
     * 
     * @param name The name of the subgraph
     * @return The subgraph
     */
    IModelSubgraph addSubgraph(String name);

    /**
     * Add subgraph with properties
     * 
     * If the subgraph already exists in the graph, only the properties are
     * updated (if applicable)
     * 
     * @param name The name of the subgraph
     * @param properties The properties of the subgraph
     * @return The subgraph
     */
    IModelSubgraph addSubgraph(String name, Map<String, String> properties);

    /**
     * Removes the subgraph from the graph and subgraphs if applicable.
     * 
     * If the subgraph contains nodes or edges, these are not deleted
     * automaticly since they also exist in the root graph as well.
     * 
     * @param subgraph The subgraph that needs to be deleted
     * @return True if the subgraph is removed, false otherwise.
     */
    boolean removeSubgraph(IModelSubgraph subgraph);

    /**
     * Removes the subgraph from the graph and subgraphs if applicable.
     * 
     * If the subgraph contains nodes or edges, these are not deleted
     * automaticly since they also exist in the root graph as well.
     * 
     * @param subgraphId The id of the subgraph
     * @return True if the subgraph is removed, false otherwise
     */
    boolean removeSubgraph(Integer subgraphId);

    /**
     * Removes the subgraph from the graph and subgraphs if applicable.
     * 
     * If the subgraph contains nodes or edges, these are not deleted
     * automaticly since they also exist in the root graph as well.
     * 
     * @param name The name of the subgraph
     * @return True if subgraph is removed, false otherwise
     */
    boolean removeSubgraph(String name);

    /**
     * Check if the graph contains an edge
     * 
     * @param edge The edge
     * @return True if graph contains the edge, false otherwise
     */
    boolean containsEdge(IModelEdge edge);

    /**
     * Check if the graph contains an edge
     * 
     * @param edgeId The id of the edge
     * @return True if graph contains the edge, false otherwise
     */
    boolean containsEdge(Integer edgeId);

    /**
     * Check if the graph contains a node
     * 
     * @param node The node
     * @return True if graph contains the node, false otherwise
     */
    boolean containsNode(IModelNode node);

    /**
     * Check if the graph contains a node
     * 
     * @param name The name of the node
     * @return True if graph contains the node, false otherwise
     */
    boolean containsNode(String name);

    /**
     * Check if graph contains a node. Using an id instead of the name will
     * require a lot of expensive lookups
     * 
     * @param name
     * @return A boolean indicating whether the graph contains the node.
     * @deprecated Bad performance. Use containsNode(String name) instead.
     */
    boolean containsNode(Integer nodeId);

    /**
     * Check if the graph contains a subgraph
     * 
     * @param subgraph The subgraph
     * @return True if graph contains the subgraph, false otherwise
     */
    boolean containsSubgraph(IModelSubgraph subgraph);

    /**
     * Check if the graph contains a subgraph
     * 
     * @param subgraphId The id of the subgraph
     * @return True if graph contains the subgraph, false otherwise
     */
    boolean containsSubgraph(Integer subgraphId);

    /**
     * Check if the graph contains a subgraph
     * 
     * @param name The unique name of the subgraph
     * @return True if graph contains the subgraph, false otherwise
     */
    boolean containsSubgraph(String name);

    /**
     * Returns a node
     * 
     * @param name The unique name of the node
     * @return The requested node
     */
    IModelNode getNode(String name);

    /**
     * Returns a node
     * 
     * @param nodeId The id of the node
     * @return The requested node
     * @deprecated Bad performance. Use getNode(String name) instead.
     */
    IModelNode getNode(Integer nodeId);

    /**
     * Returns all nodes in the graph.
     * 
     * @return All nodes of the graph
     */
    Collection<IModelNode> getNodes();

    /**
     * Returns all direct nodes in the graph. These are the nodes who's parent
     * equals the graph. Nodes is subgraphs are not included.
     * 
     * @return List of all direct nodes
     */
    List<IModelNode> getDirectNodes();

    /**
     * Returns an edge
     * 
     * @param edgeId The id of the edge
     * @return The requested edge
     */
    IModelEdge getEdge(Integer edgeId);

    /**
     * Returns all edges
     * 
     * @return All edges of graph
     */
    Collection<IModelEdge> getEdges();

    IModelGraph getGraph();

    /**
     * Returns a subgraph
     * 
     * @param subgraphId The id of the subgraph
     * @return The requested edge
     */
    IModelSubgraph getSubgraph(Integer subgraphId);

    /**
     * Returns a subgraph
     * 
     * @param name The unique name of the subgraph
     * @return The requested subgraph
     */
    IModelSubgraph getSubgraph(String name);

    /**
     * Returns all subgraphs in the graph.
     * 
     * @return All subgraphs of the graph
     */
    Collection<IModelSubgraph> getSubgraphs();

    /**
     * Returns all direct subgraphs in the graph. These are the subgraphs who's
     * parent equals the graph.
     * 
     * @return List of subgraphs
     */
    List<IModelSubgraph> getDirectSubgraphs();

    /**
     * For identification purposes
     * 
     * @return the Identifier of the graph
     */
    Integer getId();

    /**
     * Retrieve the name of the graph
     * 
     * @return the name of the graph
     */
    String getName();

    /**
     * Sets the name of the graph
     * 
     * @param name The name of the graph
     */
    void setName(String name);

    /**
     * Check if the graph is a subgraph
     * 
     * @return True if the graph is a subgraph, false otherwise
     */
    boolean isSubgraph();

    /**
     * Clear subgraphs, nodes, edges and properties
     */
    void clearGraph();

    /**
     * Add a node to the graph.
     * 
     * If the node already exists in the graph, only the properties are updated
     * (if applicable). Use the value as a name
     * 
     *
     * @param label The unique label of the node 
     * @param value The value of the node 
     * @return The node
     */
	IModelNode addNode(String label, IValue value);

}