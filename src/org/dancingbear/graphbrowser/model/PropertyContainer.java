/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @see IPropertyContainer
 */
class PropertyContainer implements IPropertyContainer {

    /**
     * Collection of default properties.
     */
    private Map<String, String> defaultProperties;

    /**
     * Collection of contained properties.
     */
    private Map<String, String> containedProperties = new Hashtable<String, String>();

    /**
     * Property change listeners.
     */
    private PropertyChangeSupport listeners = new PropertyChangeSupport(this);

    /**
     * Initiate property container with a collection of default properties.
     * 
     * @param defaultProperties Collection of default properties
     */
    public PropertyContainer(Map<String, String> defaultProperties) {
        this.defaultProperties = defaultProperties;
    }

    public boolean addProperties(Map<String, String> properties) {
        if (properties != null) {
            Map<String, String> nonDefaultProperties = filterDefaultProperties(properties);
            containedProperties.putAll(nonDefaultProperties);
            return true;
        }

        return false;
    }

    public boolean setProperties(Map<String, String> properties) {
        if (properties != null) {
            Map<String, String> nonDefaultProperties = filterDefaultProperties(properties);
            this.containedProperties = nonDefaultProperties;
            return true;
        }

        return false;
    }

    public Map<String, String> getProperties() {
        Map<String, String> properties = new Hashtable<String, String>();
        properties.putAll(defaultProperties);
        properties.putAll(containedProperties);
        return properties;
    }

    public Map<String, String> getDefaultProperties() {
        return defaultProperties;
    }

    public boolean setDefaultProperties(Map<String, String> defaultProps) {
        if (defaultProps != null) {
            this.defaultProperties = defaultProps;
            return true;
        }

        return false;
    }

    public Map<String, String> getNonDefaultProperties() {
        return containedProperties;
    }

    public String getProperty(String key) {
        if (containedProperties.containsKey(key)) {
            return containedProperties.get(key);
        }

        return defaultProperties.get(key);
    }

    public boolean setProperty(String key, String value) {
        String oldValue = containedProperties.get(key);
        containedProperties.put(key, value);

        // Notify of property change
        firePropertyChange(CONTAINER_PROPERTY, oldValue, value);

        return true;
    }

    public String getNonDefaultProperty(String key) {
        return containedProperties.get(key);
    }

    public String getDefaultProperty(String key) {
        return defaultProperties.get(key);
    }

    public Set<String> getPropertyKeys() {
        Set<String> set = new HashSet<String>();
        set.addAll(containedProperties.keySet());
        set.addAll(defaultProperties.keySet());
        return set;
    }

    public Set<String> getNonDefaultPropertyKeys() {
        return containedProperties.keySet();
    }

    public Set<String> getDefaultPropertyKeys() {
        return defaultProperties.keySet();
    }

    public boolean removeProperties() {
        containedProperties.clear();
        return true;
    }

    public boolean removeProperty(String key) {
        containedProperties.remove(key);
        return true;
    }

    public boolean removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
        return true;
    }

    public boolean addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(listener);
        return true;
    }

    public void firePropertyChange(String key, Object oldValue, Object newValue) {
        listeners.firePropertyChange(key, oldValue, newValue);
    }

    /**
     * Remove all default properties from the contained property values,
     * resulting in only a collection of custom properties.
     * 
     * @param properties Properties collection
     * @return Collection of custom properties
     */
    private Map<String, String> filterDefaultProperties(
            Map<String, String> properties) {
        Map<String, String> nonDefaultProperties = new Hashtable<String, String>();
        Iterator<String> iterator = properties.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (!defaultProperties.containsKey(key)
                    || !defaultProperties.get(key).equals(properties.get(key))) {
                nonDefaultProperties.put(key, properties.get(key));
            }
        }
        return nonDefaultProperties;
    }

}