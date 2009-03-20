/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.model;

import java.util.Map;
import java.util.Set;

/**
 * Container type for map structured properties. This interface is extended by
 * all graph related elements.
 * 
 * @see IModelGraph
 * @see IModelNode
 * @see IModelSubgraph
 * @see IModelEdge
 * 
 * @author Nickolas Heirbaut
 * @author Jeroen van Schagen
 * @version $id
 */
public interface IPropertyContainer extends IPropertyPublisher {

    /**
     * Property change identifier.
     */
    String CONTAINER_PROPERTY = "ContainerProperty";

    /**
     * Attach a map structure of properties to the container.
     * 
     * @param properties Properties map
     * @return True if the properties are added, false otherwise.
     */
    boolean addProperties(Map<String, String> properties);

    /**
     * Remove all properties from container.
     * 
     * @return True if the properties are removed, false otherwise.
     */
    boolean removeProperties();

    /**
     * Remove a property from the container.
     * 
     * @param key Name of the property that needs to be removed
     * @return True if the property is removes, false otherwise.
     */
    boolean removeProperty(String key);

    /**
     * Retrieve a property value.
     * 
     * @param key Name of the property that has to be retrieved
     * @return Value of the property.
     */
    String getProperty(String key);

    /**
     * Store a property in the container.
     * 
     * @param key Name of the property
     * @param value Value of the property
     * @return True if the property has been stored, false otherwise.
     */
    boolean setProperty(String key, String value);

    /**
     * Retrieve a map structure of all properties, currently stored in the
     * container.
     * 
     * @return All properties stored in the container.
     */
    Map<String, String> getProperties();

    /**
     * Change the container properties to the specified collection of new
     * properties. All previous properties will be lost in the process.
     * 
     * @param properties Collection of properties to be stored
     * @return True if the properties are set, false otherwise.
     */
    boolean setProperties(Map<String, String> properties);

    /**
     * Retrieve the property keys of all properties currently stored in the
     * container.
     * 
     * @return Collection of property keys
     */
    Set<String> getPropertyKeys();

    /**
     * Retrieve a default property value.
     * 
     * @param key Name of the default property
     * @return Value of the default property.
     */
    String getDefaultProperty(String key);

    /**
     * Retrieve a clone of all default properties, currently stored in the
     * container.
     * 
     * @return Collection clone of all default properties.
     */
    Map<String, String> getDefaultProperties();

    /**
     * Change the container default properties to the specified collection of
     * new default properties. All previous default properties will be lost in
     * the process.
     * 
     * @param properties Collection of default properties to be stored
     * @return True if the default properties are set, false otherwise.
     */
    boolean setDefaultProperties(Map<String, String> properties);

    /**
     * Retrieve the default property keys of all properties currently stored in
     * the container.
     * 
     * @return Collection of default property keys
     */
    Set<String> getDefaultPropertyKeys();

    /**
     * Retrieve a non-default property value.
     * 
     * @param key Name of the non-default property
     * @return Value of the non-default property.
     */
    String getNonDefaultProperty(String key);

    /**
     * Retrieve a clone of all non-default properties, currently stored in the
     * container.
     * 
     * @return Collection clone of all non-default properties.
     */
    Map<String, String> getNonDefaultProperties();

    /**
     * Retrieve the non-default property keys of all non-default properties
     * currently stored in the container.
     * 
     * @return Collection of non-default property keys
     */
    Set<String> getNonDefaultPropertyKeys();

}