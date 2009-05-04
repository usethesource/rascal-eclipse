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
 * Class holds all default properties of an {@link IModelSubgraph}.
 * 
 * @see "http://www.graphviz.org/pdf/dotguide.pdf"
 * 
 * @author Taco Witte
 * @author Nickolas Heirbaut
 */
public final class DefaultSubgraphProperties {

    /**
     * Property container
     */
    private static Map<String, String> defaultProperties = initializeProperties();

    /**
     * Initialize properties
     * 
     * @return Map with default properties
     */
    private static Map<String, String> initializeProperties() {
        Map<String, String> properties = new Hashtable<String, String>();

        // Copy the default properties of graph
        properties.putAll(DefaultGraphProperties.getDefaultProperties());

        // Remove default properties of graph that are not used in subgraphs
        properties.remove("isStrict");
        properties.remove("name");
        properties.remove("type");

        // Add default properties that are specific for subgraphs
        properties.put("collapsed", "false");
        properties.put("bgcolor", "blue"); // TODO: this should change

        return properties;
    }

    /**
     * @return the defaultProperties
     */
    public static Map<String, String> getDefaultProperties() {
        return defaultProperties;
    }

    /**
     * @param defaultProperties the defaultProperties to set
     */
    public static void setDefaultProperties(
            Map<String, String> defaultProperties) {
        DefaultSubgraphProperties.defaultProperties = defaultProperties;
    }
}