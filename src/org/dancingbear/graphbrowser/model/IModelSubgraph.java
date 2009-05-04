/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.model;

/**
 * Subgraph model interface.
 * 
 * This model is the subgraph of an an (un)directed graph or other subgraph.
 * 
 * @author Nickolas Heirbaut
 * @author Jeroen Van Schagen
 */
public interface IModelSubgraph extends IModelGraph {

    /**
     * Get the root graph of the subgraph
     * 
     * @return The root graph of the subgraph
     */
    IModelGraph getGraph();

    /**
     * Get the parent graph of the subgraph
     * 
     * @return The parent graph of the subgraph
     */
    IModelGraph getParent();

    /**
     * Set the parent of the subgraph
     * 
     * @param parent The parent of the subgraph
     */
    void setParent(IModelGraph parent);

    /**
     * ?
     * 
     * @return ?
     */
    IModelGraph getRoot();

    /**
     * Get the name of the subgraph
     */
    String getName();

    /**
     * Set the name of the subgraph
     */
    void setName(String name);

    /**
     * Get the position of the subgraph Used for cluster which are laid out as a
     * nodes.
     * 
     * @return The position of the subgraph
     */
    Position getPosition();

    /**
     * Set the position of the subgraph Used for cluster which are laid out as a
     * nodes.
     * 
     * @param x The x position of the subgraph
     * @param y The y position of the subgraph
     */
    void setPosition(double x, double y);

    /**
     * Set the position of the subgraph Used for cluster which are laid out as a
     * nodes.
     * 
     * @param position The position of the subgraph
     */
    void setPosition(Position position);

    /**
     * Transforms the subgraph to a cluster
     * 
     * Changes the property clusterrank to local and the name of the subgraph to
     * begin with prefix "cluster_" (example: name=classDiagram -->
     * name=cluster_classDiagram)
     * 
     * @see "http://www.graphviz.org/doc/info/attrs.html#d:clusterrank"
     */
    public void transformToCluster();

    /**
     * Transforms the subgraph to a non-cluster.
     * 
     * Changes the property clusterrank to none
     * 
     * @see "http://www.graphviz.org/doc/info/attrs.html#d:clusterrank"
     */
    public void transformToNonCluster(boolean changeName);

    /**
     * Determines wether the subgraph is a cluster or not
     * 
     * Two properties needs to have a specific value in order to transform the
     * subgraph to a cluster.
     * 
     * If the key clusterrank in the properties is "local", a subgraph whose
     * name begins with "cluster" is given special treatment. The subgraph is
     * laid out separately, and then integrated as a unit into its parent graph,
     * with a bounding rectangle drawn about it. Note also that there can be
     * clusters within clusters. At present, the modes "global" and "none" of
     * clusterrank appear to be identical, both turning off the special cluster
     * processing. Default value of clusterrank is local.
     * 
     * @see "http://www.graphviz.org/doc/info/attrs.html#d:clusterrank"
     * 
     * @return True the subgraph is a cluster
     */
    boolean isCluster();

}