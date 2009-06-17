/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Subgraph implementation, based on graph implementation.
 * 
 * @author Jeroen van Lieshout
 * @author Taco Witte
 */
class ModelSubgraph extends ModelGraph implements IModelSubgraph {

    private IModelGraph rootGraph;
    private IModelGraph parent;

    private List<Integer> subNodes;
    private List<Integer> subSubgraphs;

    private Position nodePosition;

    private static final String KEY_CLUSTERRANK = "clusterrank";
    private static final String VALUE_CLUSTERRANK_ENABLED = "local";
    private static final String VALUE_CLUSTERRANK_DISABLED = "none";
    private static final String PREFIX_CLUSTER = "cluster";

    public ModelSubgraph(IModelGraph root, IModelGraph parentGraph,
            Integer subId, String name) {
        this(root, parentGraph, subId, name, new Hashtable<String, String>());
    }

    public ModelSubgraph(IModelGraph root, IModelGraph parentGraph,
            Integer subId, String name, Map<String, String> properties) {
        super(subId, name);

        rootGraph = root;
        parent = parentGraph;

        // Override the default properties of the parent graph with default
        // properties of subgraph. (not possible to do this in
        // constructor)
        setDefaultProperties(DefaultSubgraphProperties.getDefaultProperties());
        // Override the default properties of the subgraph with local properties
        setProperties(properties);

        subNodes = new ArrayList<Integer>();
        subSubgraphs = new ArrayList<Integer>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.ModelGraph#addEdge(org.dancingbear
     * .graphbrowser.model.IModelNode,
     * org.dancingbear.graphbrowser.model.IModelNode)
     */
    @Override
    public IModelEdge addEdge(IModelNode source, IModelNode target)
            throws NoSuchElementException {
        return rootGraph.addEdge(source, target);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.ModelGraph#addEdge(org.dancingbear
     * .graphbrowser.model.IModelNode,
     * org.dancingbear.graphbrowser.model.IModelNode, java.util.Map)
     */
    @Override
    public IModelEdge addEdge(IModelNode source, IModelNode target,
            Map<String, String> properties) throws NoSuchElementException {
        return rootGraph.addEdge(source, target, properties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.ModelGraph#addEdge(java.lang.String,
     * java.lang.String)
     */
    @Override
    public IModelEdge addEdge(String sourceName, String targetName)
            throws NoSuchElementException {
        return rootGraph.addEdge(sourceName, targetName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.ModelGraph#addEdge(java.lang.String,
     * java.lang.String, java.util.Map)
     */
    @Override
    public IModelEdge addEdge(String sourceName, String targetName,
            Map<String, String> properties) throws NoSuchElementException {
        return rootGraph.addEdge(sourceName, targetName, properties);
    }

    /**
     * Adds a node to the subgraph and its parent if it isn't already known
     * there
     * 
     * @param name The name of the node
     * @return The node
     */
    @Override
    public IModelNode addNode(String name) {
		return addNode(this, name, null, null);
    }

    /**
     * Adds a node to the subgraph with properties and its parent if it isn't
     * already known there
     * 
     * @param name The name of the node
     * @param properties The properties of the node
     * @return The node
     */
    @Override
    public IModelNode addNode(String name, Map<String, String> properties) {
        IModelNode node = rootGraph.addNode(this, name, properties);
        if (!subNodes.contains(node.getId())) {
            subNodes.add(node.getId());
        }
        return node;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.ModelGraph#addSubgraph(java.lang.String
     * )
     */
    @Override
    public IModelSubgraph addSubgraph(String name) {
        return addSubgraph(name, null);
    }

    @Override
    public IModelSubgraph addSubgraph(String name,
            Map<String, String> properties) {
        IModelSubgraph subgraph = rootGraph.addSubgraph(this, name, properties);
        subgraph.setParent(this);
        if (!subSubgraphs.contains(subgraph.getId())) {
            subSubgraphs.add(subgraph.getId());
        }
        return subgraph;
    }

    @Override
    public void clearGraph() {
        for (IModelSubgraph child : getDirectSubgraphs()) {
            child.clearGraph();
        }

        parent = null;

        subNodes.clear();
        subSubgraphs.clear();
    }

    /**
     * Confirms whether the edge is part of the overall graph and that both
     * source and target node are contained in the subgraph.
     * 
     * @param edge The edge under scrutiny
     * @return True if the edge totally within the subgraph, false otherwise
     */
    @Override
    public boolean containsEdge(IModelEdge edge) {
        if (rootGraph.containsEdge(edge)) {
            if (subNodes.contains(edge.getSource().getId())
                    && subNodes.contains(edge.getTarget().getId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Confirms whether the edge is part of the overall graph and that both
     * source and target node are contained in the subgraph.
     * 
     * @param edgeId The edge under scrutiny
     * @return True if the edge totally within the subgraph, false otherwise
     */
    @Override
    public boolean containsEdge(Integer edgeId) {
        return containsEdge(rootGraph.getEdge(edgeId));
    }

    /**
     * Confirms whether the node is part of the overall graph and subgraph
     * 
     * @param node The node under scrutiny
     * @return True if the node is in the subgraph, false otherwise
     */
    @Override
    public boolean containsNode(IModelNode node) {
        if (rootGraph.containsNode(node)) {
            return subNodes.contains(node.getId());
        }
        return false;
    }

    /**
     * Determines wether the subgraph is a cluster or not
     * 
     * Two properties needs to have a specific value in order to transform the
     * subgraph to a cluster.
     * 
     * If the key clusterrank is "local", a subgraph whose name begins with
     * "cluster" is given special treatment. The subgraph is laid out
     * separately, and then integrated as a unit into its parent graph, with a
     * bounding rectangle drawn about it. Note also that there can be clusters
     * within clusters. At present, the modes "global" and "none" of clusterrank
     * appear to be identical, both turning off the special cluster processing.
     * Default value of clusterrank is local.
     * 
     * @see "http://www.graphviz.org/doc/info/attrs.html#d:clusterrank"
     * 
     * @return True the subgraph is a cluster
     */
    public boolean isCluster() {
        boolean isCluster = this.getName().startsWith(PREFIX_CLUSTER)
                && VALUE_CLUSTERRANK_ENABLED.equals(this
                        .getProperty(KEY_CLUSTERRANK));
        return isCluster;
    }

    /**
     * Transforms the subgraph to a cluster
     * 
     * Changes the property clusterrank to local and the name of the subgraph to
     * begin with prefix "cluster_XXXXX" (example: name=classDiagram -->
     * name=cluster_classDiagram) where XXXXX is the original name.
     * 
     * @see "http://www.graphviz.org/doc/info/attrs.html#d:clusterrank"
     */
    public void transformToCluster() {
        this.setProperty(KEY_CLUSTERRANK, VALUE_CLUSTERRANK_ENABLED);
        if (!this.getName().startsWith(PREFIX_CLUSTER)) {
            this.setName(PREFIX_CLUSTER + "_" + this.getName());
        }
    }

    /**
     * Transforms the subgraph to a non-cluster
     * 
     * Changes the property clusterrank to none
     * 
     * @see "http://www.graphviz.org/doc/info/attrs.html#d:clusterrank"
     */
    public void transformToNonCluster(boolean changeName) {
        this.setProperty(KEY_CLUSTERRANK, VALUE_CLUSTERRANK_DISABLED);
        if (changeName && this.getName().startsWith(PREFIX_CLUSTER)) {
            this.setName(this.getName().replace(PREFIX_CLUSTER + "_", ""));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (((IModelGraph) obj).isSubgraph()) {
                return ((IModelSubgraph) obj).getRoot().equals(rootGraph);
            }
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<IModelNode> getDirectNodes() {
        List<IModelNode> children = new ArrayList<IModelNode>();

        for (Integer nodeId : subNodes) {
            IModelNode node = rootGraph.getNode(nodeId);
            children.add(node);
        }

        return children;
    }

    @Override
    public List<IModelSubgraph> getDirectSubgraphs() {
        List<IModelSubgraph> children = new ArrayList<IModelSubgraph>();
        for (IModelSubgraph subgraph : parent.getSubgraphs()) {
            if (subSubgraphs.contains(subgraph.getId())) {
                if (subgraph.getParent().equals(this)) {
                    children.add(subgraph);
                }
            }
        }

        return children;
    }

    /**
     * Retrieve the edge
     * 
     * @return The edge, or null if it is not known in the graph or not totally
     * in the subgraph
     */
    @Override
    public IModelEdge getEdge(Integer edgeId) {
        if (containsEdge(edgeId)) {
            return rootGraph.getEdge(edgeId);
        }

        return null;
    }

    /**
     * Retrieves all edges that are totally within the subgraph
     * 
     * @return A list of the edges in the subgraph
     */
    @Override
    public List<IModelEdge> getEdges() {
        List<IModelEdge> edges = new ArrayList<IModelEdge>();
        for (IModelEdge edge : rootGraph.getEdges()) {
            if (this.containsEdge(edge)) {
                edges.add(edge);
            }
        }

        return edges;
    }

    // /**
    // * Retrieves the node from the subgraph
    // *
    // * @return The node, or null if it is not part of the graph and subgraph
    // */
    // @Override
    // public IModelNode getNode(String name) {
    // return parent.getNode(name);
    // }

    /**
     * Retrieves all nodes that are part of this subgraph
     * 
     * @return A list of the nodes in the subgraph
     */
    @Override
    public List<IModelNode> getNodes() {
        List<IModelNode> nodes = new ArrayList<IModelNode>();
        for (IModelNode node : rootGraph.getNodes()) {
            if (subNodes.contains(node.getId())) {
                nodes.add(node);
            }
        }

        return nodes;
    }

    @Override
    public IModelNode getNode(String name) {
        IModelNode node = rootGraph.getNode(name);
        if (node != null && subNodes.contains(node.getId())) {
            return node;
        }

        return null;
    }

    public void setParent(IModelGraph parent) {
        this.parent = parent;
    }

    public IModelGraph getParent() {
        return parent;
    }

    public IModelGraph getRoot() {
        return rootGraph;
    }

    /**
     * Retrieves the subsubgraph from this subgraph
     * 
     * @return The subsubgraph, or null if it is not part of the graph and
     * subgraph
     */
    @Override
    public IModelSubgraph getSubgraph(String name) {
        IModelSubgraph subgraph = rootGraph.getSubgraph(name);
        if (subgraph != null && subgraph.getParent().equals(this)) {
            return subgraph;
        }
        return null;
    }

    @Override
    public IModelSubgraph getSubgraph(Integer subgraphId) {
        IModelSubgraph subgraph = rootGraph.getSubgraph(subgraphId);
        if (subgraph != null && subgraph.getParent().equals(this)) {
            return subgraph;
        }
        return null;
    }

    /**
     * Retrieves all nodes that are part of this subgraph
     * 
     * @return A list of the nodes in the subgraph
     */
    @Override
    public List<IModelSubgraph> getSubgraphs() {
        List<IModelSubgraph> subsubs = new ArrayList<IModelSubgraph>();
        for (IModelSubgraph subsub : rootGraph.getSubgraphs()) {
            if (subSubgraphs.contains(subsub.getId())) {
                subsubs.add(subsub);
            }
        }

        return subsubs;
    }

    @Override
    public int hashCode() {
        // contractual necessity when overriding equals
        return getIdentifier().intValue();
    }

    @Override
    public boolean isSubgraph() {
        return true;
    }

    /**
     * @return always false, edge is not a part of a subgraph
     */
    @Override
    public boolean removeEdge(IModelEdge edge) {
        return false;
    }

    /**
     * @return always false, edge is not a part of a subgraph
     */
    @Override
    public boolean removeEdge(Integer edgeId) {
        return false;
    }

    /**
     * Removes the node from the subgraph and its children
     * 
     * @return True if successful, false otherwise or if the node is not part of
     * the subgraph
     */
    @Override
    public boolean removeNode(IModelNode node) {
        return rootGraph.removeNode(node);
    }

    /**
     * Removes the node from the subgraph and its children
     * 
     * @return True if successful, false otherwise or if the node is not part of
     * the subgraph
     */
    @Override
    public boolean removeNode(String name) {
        return rootGraph.removeNode(name);
    }

    /**
     * Removes the subsubgraph from this subgraph and its children
     * 
     * @return True if successful, false otherwise or if the subsubgraph is not
     * part of the subgraph
     */
    @Override
    public boolean removeSubgraph(IModelSubgraph subgraph) {
        if (containsSubgraph(subgraph)) {
            for (IModelSubgraph child : getDirectSubgraphs()) {
                child.removeSubgraph(subgraph);
            }
            return subSubgraphs.remove(subgraph.getId());
        }

        return false;
    }

    /**
     * Removes the subsubgraph from this subgraph and its children
     * 
     * @return True if successful, false otherwise or if the subsubgraph is not
     * part of the subgraph
     */
    @Override
    public boolean removeSubgraph(Integer subgraphId) {
        return removeSubgraph(rootGraph.getSubgraph(subgraphId));
    }

    @Override
    public boolean containsSubgraph(IModelSubgraph subgraph) {
        if (subgraph != null) {
            return subSubgraphs.contains(subgraph.getId());
        }
        return false;
    }

    @Override
    public boolean containsSubgraph(Integer subgraphId) {
        return subSubgraphs.contains(subgraphId);
    }

    @Override
    public boolean containsSubgraph(String name) {
        IModelSubgraph subgraph = getSubgraph(name);
        if (subgraph != null) {
            return subSubgraphs.contains(subgraph.getId());
        }
        return false;
    }

    /**
     * Removes the subsubgraph from this subgraph and its children
     * 
     * @return True if successful, false otherwise or if the subsubgraph is not
     * part of the subgraph
     */
    @Override
    public boolean removeSubgraph(String name) {
        return removeSubgraph(rootGraph.getSubgraph(name));
    }

    /**
     * Sets the name of this subgraph
     * 
     * @param graphName The new name
     */
    public void setName(String graphName) {
        super.setName(graphName);
    }

    public void setPosition(double x, double y) {
        setPosition(new Position(x, y));
    }

    public void setPosition(Position position) {
        Position oldPosition = this.nodePosition;
        this.nodePosition = position;

        // Notify listeners of position change
        firePropertyChange(IModelNode.NODE_LAYOUT, oldPosition, position);
    }

    public Position getPosition() {
        return nodePosition;
    }

}
