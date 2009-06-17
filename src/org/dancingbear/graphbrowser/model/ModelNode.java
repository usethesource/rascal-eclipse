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

import org.eclipse.imp.pdb.facts.IValue;

class ModelNode extends PropertyContainer implements IModelNode {

    private IModelGraph parent;
    private final Integer identifier;
    private String name;

    private Position nodePosition;
	private IValue value;
	
	public ModelNode(IModelGraph graph, Integer nodeId, String nodeName, IValue value) {
        this(graph, nodeId, nodeName, new Hashtable<String, String>(), value);
    }

    public ModelNode(IModelGraph graph, Integer nodeId, String nodeName) {
        this(graph, nodeId, nodeName, new Hashtable<String, String>(), null);
    }

    public ModelNode(IModelGraph parent, Integer nodeId, String nodeName,
            Map<String, String> properties, IValue value) {
        super(DefaultNodeProperties.getDefaultProperties());
        this.parent = parent;
        identifier = nodeId;
        name = nodeName;

        setProperties(properties);

        nodePosition = new Position();
        this.value = value;
    }

    public IModelEdge addIncomingEdge(IModelNode sourceNode) {
        IModelEdge edge = parent.addEdge(sourceNode, this);

        firePropertyChange(NODE_INCOMING, null, edge);

        return edge;
    }

    public IModelEdge addIncomingEdge(String sourceNode) {
        return addIncomingEdge(parent.getNode(sourceNode));
    }

    public IModelEdge addOutgoingEdge(IModelNode targetNode) {
        IModelEdge edge = parent.addEdge(this, targetNode);

        firePropertyChange(NODE_OUTGOING, null, edge);

        return edge;
    }

    public IModelEdge addOutgoingEdge(String targetNode) {
        return addOutgoingEdge(parent.getNode(targetNode));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModelNode) {
            IModelNode compareNode = (IModelNode) obj;
            if (identifier.equals(compareNode.getId())) {
                return compareNode.getParentGraph().equals(parent);
            }
        }

        return false;
    }

    public IModelGraph getParentGraph() {
        return parent;
    }

    public Integer getId() {
        return identifier;
    }

    public IModelEdge getIncomingEdge(Integer edgeId) {
        IModelEdge edge = parent.getEdge(edgeId);
        if (edge != null && edge.getTarget().equals(this)) {
            return edge;
        }

        return null;
    }

    public List<IModelEdge> getIncomingEdges() {
        List<IModelEdge> edges = new ArrayList<IModelEdge>();
        for (IModelEdge edge : parent.getEdges()) {
            if (edge.getTarget().equals(this)) {
                edges.add(edge);
            }
        }

        return edges;
    }

    public String getName() {
        return name;
    }

    public IModelEdge getOutgoingEdge(Integer edgeId) {
        IModelEdge edge = parent.getEdge(edgeId);
        if (edge != null && edge.getSource().equals(this)) {
            return edge;
        }

        return null;
    }

    public List<IModelEdge> getOutgoingEdges() {
        List<IModelEdge> edges = new ArrayList<IModelEdge>();
        for (IModelEdge edge : parent.getEdges()) {
            if (this.equals(edge.getSource())) {
                edges.add(edge);
            }
        }

        return edges;
    }

    public Position getPosition() {
        return nodePosition;
    }

    @Override
    public int hashCode() {
        // Contractual necessity when overriding equals
        return identifier.intValue();
    }

    public boolean removeIncomingEdge(IModelEdge edge) {
        if (edge != null) {
            if (this.equals(edge.getTarget())) {
                boolean removeSuccess = parent.removeEdge(edge);
                if (removeSuccess) {
                    firePropertyChange(NODE_INCOMING, edge, null);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean removeIncomingEdge(Integer edgeId) {
        if (parent.containsEdge(edgeId)) {
            return removeIncomingEdge(getIncomingEdge(edgeId));
        }

        return false;
    }

    public boolean removeOutgoingEdge(IModelEdge edge) {
        if (edge != null) {
            if (this.equals(edge.getSource())) {
                boolean removeSuccess = parent.removeEdge(edge);
                if (removeSuccess) {
                    firePropertyChange(NODE_OUTGOING, edge, null);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean removeOutgoingEdge(Integer edgeId) {
        if (parent.containsEdge(edgeId)) {
            return removeOutgoingEdge(getOutgoingEdge(edgeId));
        }

        return false;
    }

    public void setPosition(double x, double y) {
        setPosition(new Position(x, y));
    }

    public void setPosition(Position position) {
        Position oldPosition = this.nodePosition;
        this.nodePosition = position;

        // Notify listeners of position change
        firePropertyChange(NODE_LAYOUT, oldPosition, position);
    }

    public IModelGraph getParent() {
        return this.parent;
    }

    public void setParent(IModelGraph parent) {
        this.parent = parent;
    }

	public IValue getValue() {
		return value;
	}

}