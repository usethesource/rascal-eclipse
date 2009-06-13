/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.model;

import java.util.Hashtable;
import java.util.Map;

class ModelEdge extends PropertyContainer implements IModelEdge {

    private final IModelGraph modelGraph;

    private final Integer identifier;
    private IModelNode edgeSource;
    private IModelNode edgeTarget;

    private Spline edgeSpline;

    public ModelEdge(IModelGraph graph, Integer edgeId, IModelNode source,
            IModelNode target) {
        this(graph, edgeId, source, target, new Hashtable<String, String>());
    }

    public ModelEdge(IModelGraph graph, Integer edgeId, IModelNode source,
            IModelNode target, Map<String, String> properties) {
        super(DefaultEdgeProperties.getDefaultProperties());
        modelGraph = graph;
        identifier = edgeId;

        setProperties(properties);

        // Arrow heads should be disabled in an undirected graph, except if one
        // is specified in the properties.
        if ("graph".equals(modelGraph.getProperty("type"))
                && properties.get("arrowhead") == null) {
            this.setProperty("arrowhead", "none");
        }

        edgeSource = source;
        edgeTarget = target;
        edgeSpline = new Spline();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModelEdge) {
            IModelEdge compareEdge = (IModelEdge) obj;
            if (compareEdge.getId().equals(identifier)) {
                return compareEdge.getGraph().equals(modelGraph);
            }
        }
        return false;
    }

    public IModelGraph getGraph() {
        return modelGraph;
    }

    public Integer getId() {
        return identifier;
    }

    public IModelNode getSource() {
        return edgeSource;
    }

    public Spline getSpline() {
        return edgeSpline;
    }

    public IModelNode getTarget() {
        return edgeTarget;
    }

    @Override
    public int hashCode() {
        return identifier.intValue();
    }

    public boolean setSource(IModelNode newSource) {
        if (modelGraph.containsNode(newSource)) {
            IModelNode oldSource = this.edgeSource;
            this.edgeSource = newSource;

            // Notify source change
            firePropertyChange(IModelEdge.EDGE_SOURCE, oldSource, newSource);
            oldSource.firePropertyChange(IModelNode.NODE_OUTGOING, this, null);
            newSource.firePropertyChange(IModelNode.NODE_OUTGOING, null, this);

            return true;
        }

        return false;
    }

    public boolean setSource(String name) {
        if (modelGraph.containsNode(name)) {
            return setSource(modelGraph.getNode(name));
        }

        return false;
    }

    public boolean setSpline(Spline spline) {
        edgeSpline = spline;

        // Notify spline change
        firePropertyChange(IModelEdge.EDGE_SPLINE, null, edgeSpline);

        return true;
    }

    public boolean setTarget(IModelNode newTarget) {
        if (modelGraph.containsNode(newTarget)) {
            IModelNode oldTarget = this.edgeTarget;
            this.edgeTarget = newTarget;

            // Notify target change
            firePropertyChange(IModelEdge.EDGE_TARGET, oldTarget, newTarget);
            oldTarget.firePropertyChange(IModelNode.NODE_INCOMING, this, null);
            newTarget.firePropertyChange(IModelNode.NODE_INCOMING, null, this);

            return true;
        }

        return false;
    }

    public boolean setTarget(String name) {
        if (modelGraph.containsNode(name)) {
            return setTarget(modelGraph.getNode(name));
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dancingbear.graphbrowser.model.PropertyContainer#firePropertyChange
     * (java.lang.String, java.lang.Object, java.lang.Object)
     */
    @Override
    public void firePropertyChange(String key, Object oldValue, Object newValue) {
        super.firePropertyChange(key, oldValue, newValue);

        if (oldValue instanceof IModelEdge) {
            IModelEdge oldEdge = (IModelEdge) oldValue;

            oldEdge.getSource().firePropertyChange(EDGE_SOURCE, oldValue,
                    newValue);
            oldEdge.getTarget().firePropertyChange(EDGE_TARGET, oldValue,
                    newValue);
        }
    }
}
