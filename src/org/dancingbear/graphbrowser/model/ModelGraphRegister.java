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

/**
 * Register for creating graphs and storing graphs in memory
 * 
 * @author Jeroen van Schagen
 * @author Joppe Kroon
 * 
 */

public final class ModelGraphRegister {

    private Hashtable<String, IModelGraph> modelGraphs;

    private int graphId;

    private static ModelGraphRegister instance = null;

    /**
     * Retrieve the singleton instance of this register, or create it if it is
     * not known
     * 
     * @return The singleton instance of this register
     */
    public static ModelGraphRegister getInstance() {
        if (instance == null) {
            instance = new ModelGraphRegister();
        }

        return instance;
    }

    private ModelGraphRegister() {
        modelGraphs = new Hashtable<String, IModelGraph>();
        graphId = 0;
    }

    /**
     * Creates a new graph identifier
     * 
     * @return the graph identifier
     */
    private Integer createGraphId() {
        graphId++;
        return Integer.valueOf(graphId);
    }

    /**
     * Force to create new graph with specified name
     * 
     * @param name Name of graph
     * @return graph Created graph
     */
    public IModelGraph forceNewGraph(String name) {
        return forceNewGraph(name, null);
    }

    /**
     * Force to create new graph with specified name and properties
     * 
     * @param name Name of graph
     * @param properties Properties of graph
     * @return graph Created graph
     */
    public IModelGraph forceNewGraph(String name, Map<String, String> properties) {
        if (modelGraphs.containsKey(name)) {
            modelGraphs.get(name).clearGraph();
            modelGraphs.remove(name);
        }

        return getModelGraph(name, properties);
    }

    /**
     * Get request graph
     * 
     * @param name Name of graph
     * @return graph Requested graph
     */
    public IModelGraph getModelGraph(String name) {
        return getModelGraph(name, null);
    }

    /**
     * Request whether a graph already exists in the register
     * 
     * @param name The (file)name of the graph
     * @return True if it exists, false otherwise
     */
    public boolean isGraphOpen(String name) {
        return modelGraphs.containsKey(name);
    }

    /**
     * Retrieve a modelGraph by name or create a new one if it doesn't exist
     * 
     * @param name the (file)name of the graph
     * @return a graph associated to the name
     */
    public IModelGraph getModelGraph(String name, Map<String, String> properties) {
        if (!modelGraphs.containsKey(name)) {
            modelGraphs.put(name, new ModelGraph(createGraphId(), name,
                    properties));
        }

        return modelGraphs.get(name);
    }

    /**
     * Remove a graph from the register.
     * 
     * NOTE: Be careful that all instances of the graph are closed beforehand.
     * 
     * @param name The name of the graph to remove
     * @return True if successful, false otherwise
     */
    public boolean removeGraph(String name) {
        // TODO is there a possibility of adding a listener to a graph to be
        // certain it actually has no open instances?
        if (modelGraphs.remove(name) == null) {
            return false;
        }

        return true;
    }
}