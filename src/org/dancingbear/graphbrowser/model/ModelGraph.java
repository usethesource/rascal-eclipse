/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 
 * Default graph implementation
 * 
 * @version $Id: ModelGraph.java 1299 2009-03-16 16:56:36Z nickolas.heirbaut $
 */
class ModelGraph extends PropertyContainer implements IModelGraph {

    private final Integer identifier;
    private String graphName;

    private int edgeIdCounter = 0;
    private int nodeIdCounter = 0;

    private Map<Integer, IModelEdge> edges;
    private Map<String, IModelNode> nodes;
    private Map<Integer, IModelSubgraph> subgraphs;

    /**
     * Construct new graph without properties
     * 
     * @param graphId The identifier for the graph
     * @param name Name of graph
     */
    public ModelGraph(Integer graphId, String name) {
        this(graphId, name, new Hashtable<String, String>());
    }

    /**
     * Construct new graph with (possibly empty) properties
     * 
     * @param graphId Id of graph
     * @param name Name of graph
     * @param properties Properties of graph
     */
    public ModelGraph(Integer graphId, String name,
            Map<String, String> properties) {
        super(DefaultGraphProperties.getDefaultProperties());
        graphName = name;
        identifier = graphId;

        setProperties(properties);

        edges = new Hashtable<Integer, IModelEdge>();
        nodes = new Hashtable<String, IModelNode>();
        subgraphs = new Hashtable<Integer, IModelSubgraph>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#addEdge(org.dancingbear
     * .graphbrowser.model.IModelNode,
     * org.dancingbear.graphbrowser.model.IModelNode)
     */
    public IModelEdge addEdge(IModelNode source, IModelNode target)
            throws NoSuchElementException {
        return addEdge(source, target, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#addEdge(org.dancingbear
     * .graphbrowser.model.IModelNode,
     * org.dancingbear.graphbrowser.model.IModelNode, java.util.Map)
     */
    public IModelEdge addEdge(IModelNode source, IModelNode target,
            Map<String, String> properties) throws NoSuchElementException {
        hasNodes(source, target);

        IModelEdge edge = new ModelEdge(this, createEdgeId(), source, target,
                properties);
        edges.put(edge.getId(), edge);
        firePropertyChange(IModelGraph.GRAPH_EDGE, null, edge);

        return edge;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#addEdge(java.lang.String,
     * java.lang.String)
     */
    public IModelEdge addEdge(String sourceName, String targetName)
            throws NoSuchElementException {
        return addEdge(sourceName, targetName, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#addEdge(java.lang.String,
     * java.lang.String, java.util.Map)
     */
    public IModelEdge addEdge(String sourceName, String targetName,
            Map<String, String> properties) throws NoSuchElementException {
        return addEdge(getNode(sourceName), getNode(targetName), properties);
    }

    public IModelNode addNode(IModelGraph parent, String name,
            Map<String, String> properties) {
        IModelNode node = getNode(name);
        if (node == null) {
            node = new ModelNode(parent, createNodeId(), name, properties);
            nodes.put(name, node);
        } else {
            node.addProperties(properties);
            node.setParent(parent);
        }
        firePropertyChange(IModelGraph.GRAPH_NODE, null, node);
        return node;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#addNode(java.lang.String)
     */
    public IModelNode addNode(String name) {
        return addNode(name, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#addNode(java.lang.String,
     * java.util.Map)
     */
    public IModelNode addNode(String name, Map<String, String> properties) {
        return addNode(this, name, properties);
    }

    public IModelSubgraph addSubgraph(IModelGraph parent, String name,
            Map<String, String> properties) {
        IModelSubgraph subgraph = getSubgraph(name);
        if (subgraph == null) {
            subgraph = new ModelSubgraph(this, parent, createSubGraphId(),
                    name, properties);
            subgraphs.put(subgraph.getId(), subgraph);
        } else {
            subgraph.addProperties(properties);
        }
        firePropertyChange(IModelGraph.GRAPH_SUBGRAPH, null, subgraph);
        return subgraph;
    }

    public IModelSubgraph addSubgraph(String name) {
        return addSubgraph(name, null);
    }

    public IModelSubgraph addSubgraph(String name,
            Map<String, String> properties) {
        return addSubgraph(this, name, properties);
    }

    public void clearGraph() {
        for (IModelSubgraph child : getDirectSubgraphs()) {
            child.clearGraph();
        }

        subgraphs.clear();
        nodes.clear();
        edges.clear();

        edgeIdCounter = 0;
        nodeIdCounter = 0;
        // subgraphIdCounter = 0;

        removeProperties();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#containsEdge(org.dancingbear
     * .graphbrowser.model.IModelEdge)
     */
    public boolean containsEdge(IModelEdge edge) {
        boolean contains = false;
        if (edge != null && edges.containsKey(edge.getId())) {
            contains = edges.get(edge.getId()).equals(edge);
        }
        return contains;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#containsEdge(java.lang
     * .Integer)
     */
    public boolean containsEdge(Integer edgeId) {
        return edges.containsKey(edgeId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#containsNode(org.dancingbear
     * .graphbrowser.model.IModelNode)
     */
    public boolean containsNode(IModelNode node) {
        boolean contains = false;
        if (node != null && nodes.containsKey(node.getName())) {
            contains = nodes.get(node.getName()).equals(node);
        }

        return contains;
    }
    
    @Deprecated
    public boolean containsNode(Integer nodeId) {
        boolean contains = false;
        for (IModelNode node : getNodes()) {
            if (node.getId().equals(nodeId)) {
                contains = true;
            }
        }
        return contains;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#containsNode(java.lang
     * .Integer)
     */
    public boolean containsNode(String name) {
        return nodes.containsKey(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.dancingbear.graphbrowser.model.IModelGraph#containsSubgraph(org.
     * dancingbear.graphbrowser.model.IModelSubgraph)
     */
    public boolean containsSubgraph(IModelSubgraph subgraph) {
        boolean contains = false;
        if (subgraph != null) {
            contains = subgraphs.containsKey(subgraph.getId());
        }
        return contains;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#containsSubgraph(java.
     * lang.Integer)
     */
    public boolean containsSubgraph(Integer subgraphId) {
        return subgraphs.containsKey(subgraphId);
    }

    public boolean containsSubgraph(String name) {
        return containsSubgraph(getSubgraph(name));
    }

    /**
     * Create a new edge identifier
     * 
     * @return a fresh identifier for an edge
     */
    private Integer createEdgeId() {
        edgeIdCounter++;
        return Integer.valueOf(edgeIdCounter);
    }

    /**
     * Create a new node identifier
     * 
     * @return a fresh identifier for a node
     */
    private Integer createNodeId() {
        nodeIdCounter++;
        return Integer.valueOf(nodeIdCounter);
    }

    /**
     * Create a new subgraph identifier
     * 
     * @return a fresh identifier for a subgraph
     */
    private Integer createSubGraphId() {
        // for layout we need to differentiate between nodes and subgraphs by id
        return createNodeId();
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (obj instanceof ModelGraph) {
            isEqual = ((IModelGraph) obj).getId().equals(identifier);
        }

        return isEqual;
    }

    public List<IModelNode> getDirectNodes() {
        List<IModelNode> children = new ArrayList<IModelNode>();
        Iterator<String> keys = nodes.keySet().iterator();

        while (keys.hasNext()) {
            String nodeId = keys.next();
            IModelNode node = getNode(nodeId);
            if (node.getParentGraph().equals(this)) {
                children.add(node);
            }
        }

        return children;
    }

    public List<IModelSubgraph> getDirectSubgraphs() {
        List<IModelSubgraph> children = new ArrayList<IModelSubgraph>();
        Iterator<Integer> keys = subgraphs.keySet().iterator();

        while (keys.hasNext()) {
            Integer subgraphId = keys.next();
            IModelSubgraph subgraph = getSubgraph(subgraphId);
            if (subgraph.getParent().equals(this)) {
                children.add(subgraph);
            }
        }

        return children;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#getEdge(java.lang.Integer)
     */
    public IModelEdge getEdge(Integer edgeId) {
        for (IModelEdge edge : getEdges()) {
            if (edge.getId().equals(edgeId)) {
                return edge;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dancingbear.graphbrowser.model.IModelGraph#getEdges()
     */
    public Collection<IModelEdge> getEdges() {
        return edges.values();
    }

    public IModelGraph getGraph() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dancingbear.graphbrowser.model.IModelGraph#getId()
     */
    public Integer getId() {
        return identifier;
    }

    /**
     * @return the identifier
     */
    public Integer getIdentifier() {
        return identifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dancingbear.graphbrowser.model.IModelGraph#getName()
     */
    public String getName() {
        return graphName;
    }
    
    @Deprecated
    public IModelNode getNode(Integer nodeId) {
        for (IModelNode node : getNodes()) {
            if (node.getId().equals(nodeId)) {
                return node;
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#getNode(java.lang.Integer)
     */
    public IModelNode getNode(String name) {
        if (containsNode(name)) {
            return nodes.get(name);
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dancingbear.graphbrowser.model.IModelGraph#getNodes()
     */
    public Collection<IModelNode> getNodes() {
        return nodes.values();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#getSubgraph(java.lang.
     * Integer)
     */
    public IModelSubgraph getSubgraph(Integer subgraphId) {
        if (containsSubgraph(subgraphId)) {
            return subgraphs.get(subgraphId);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#getSubgraph(java.lang.
     * String)
     */
    public IModelSubgraph getSubgraph(String name) {
        for (IModelSubgraph subgraph : getSubgraphs()) {
            if (subgraph.getName().equals(name)) {
                return subgraph;
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dancingbear.graphbrowser.model.IModelGraph#getSubgraphs()
     */
    public Collection<IModelSubgraph> getSubgraphs() {
        return subgraphs.values();
    }

    @Override
    public int hashCode() {
        // contractual necessity when overriding equals
        return identifier.intValue();
    }

    /**
     * Check if the graph contains these two nodes for an edge
     * 
     * @param source The source node for an edge
     * @param target The target node for an edge
     * @throws NoSuchElementException if either node is not known by the graph
     */
    @SuppressWarnings("nls")
    private void hasNodes(IModelNode source, IModelNode target)
            throws NoSuchElementException {
        String error = "";
        if (!containsNode(source)) {
            if (source == null) {
                error += "source is null";
            } else {
                error += "source : " + source.getId() + " ," + source.getName();
            }
            error += "\n";
        }
        if (!containsNode(target)) {
            if (target == null) {
                error += "target is null";
            } else {
                error += "target : " + target.getId() + " ," + target.getName();
            }
            error += "\n";
        }

        if (!error.equals("")) {
            throw new NoSuchElementException(error);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dancingbear.graphbrowser.model.IModelGraph#isSubGraph()
     */
    public boolean isSubgraph() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#removeEdge(org.dancingbear
     * .graphbrowser.model.IModelEdge)
     */
    public boolean removeEdge(IModelEdge edge) {
        if (edge != null && containsEdge(edge)) {
            return removeEdge(edge.getId());
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#removeEdge(java.lang.Integer
     * )
     */
    public boolean removeEdge(Integer edgeId) {
        if (containsEdge(edgeId)) {
            IModelEdge edge = getEdge(edgeId);
            edges.remove(edgeId);

            edge.firePropertyChange(GRAPH_EDGE, edge, null);

            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#removeNode(org.dancingbear
     * .graphbrowser.model.IModelNode)
     */
    public boolean removeNode(IModelNode node) {
        if (containsNode(node) && nodes.get(node.getName()).equals(node)) {
            // Propagate
            for (IModelSubgraph subgraph : getDirectSubgraphs()) {
                subgraph.getNode(node.getName());
            }

            // Disconnect
            for (IModelEdge edge : node.getIncomingEdges()) {
                removeEdge(edge);
            }
            for (IModelEdge edge : node.getOutgoingEdges()) {
                removeEdge(edge);
            }

            // Remove
            nodes.remove(node.getName());
            firePropertyChange(IModelGraph.GRAPH_NODE, node, null);
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#removeNode(java.lang.String
     * )
     */
    public boolean removeNode(String name) {
        if (containsNode(name)) {
            return removeNode(getNode(name));
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#removeSubgraph(org.dancingbear
     * .graphbrowser.model.IModelSubgraph)
     */
    public boolean removeSubgraph(IModelSubgraph subgraph) {
        if (containsSubgraph(subgraph)
                && subgraphs.get(subgraph.getId()).equals(subgraph)) {
            for (IModelSubgraph child : getDirectSubgraphs()) {
                child.removeSubgraph(subgraph);
            }

            subgraphs.remove(subgraph.getId());
            firePropertyChange(IModelGraph.GRAPH_SUBGRAPH, subgraph, null);
            return true;
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#removeSubgraph(java.lang
     * .Integer)
     */
    public boolean removeSubgraph(Integer subgraphId) {
        IModelSubgraph subgraph = getSubgraph(subgraphId);
        if (subgraph != null) {
            return removeSubgraph(subgraph);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.IModelGraph#removeSubgraph(java.lang
     * .String)
     */
    public boolean removeSubgraph(String name) {
        IModelSubgraph subgraph = getSubgraph(name);
        if (subgraph != null) {
            return removeSubgraph(subgraph);
        }
        return false;
    }

    /**
     * @param graphName the graphName to set
     */
    public void setName(String graphName) {
        this.graphName = graphName;
    }
}