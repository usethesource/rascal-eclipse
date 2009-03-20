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
 * Class holds all default properties of an {@link IModelGraph}.
 * 
 * @see "http://www.graphviz.org/pdf/dotguide.pdf"
 * 
 * @author Nickolas Heirbaut
 */
public class DefaultGraphProperties {

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
        properties.put("bgcolor", "");
        properties.put("center", "false");
        properties.put("cluster", "false");
        properties.put("clusterrank", "local");
        properties.put("color", "black");
        properties.put("comment", "");
        properties.put("compound", "false");
        properties.put("concentrate", "false");
        properties.put("fillcolor", "black");
        properties.put("fontcolor", "black");
        properties.put("fontname", "Times-Roman");
        properties.put("fontpath", "");
        properties.put("fontsize", "14");
        properties.put("label", "");
        properties.put("labeljust", "centered");
        properties.put("labelloc", "top");
        properties.put("layers", "");
        properties.put("margin", ".5");
        properties.put("mclimit", "1.0");
        properties.put("nodesep", ".25");
        properties.put("nslimit", "");
        properties.put("nslimit1", "");
        properties.put("ordering", "");
        properties.put("orientation", "portrait");
        properties.put("page", "");
        properties.put("pagedir", "BL");
        properties.put("quantum", "");
        properties.put("rank", "");
        properties.put("rankdir", "TB");
        properties.put("ranksep", ".75");
        properties.put("ratio", "");
        properties.put("remincross", "");
        properties.put("rotate", "");
        properties.put("samplepoints", "8");
        properties.put("searchsize", "30");
        properties.put("size", "");
        properties.put("style", "");
        properties.put("URL", "");
        properties.put("isStrict", "false");
        properties.put("type", "digraph");
        return properties;
    }

    /**
     * Get default properties
     * 
     * @return the defaultProperties
     */
    public static Map<String, String> getDefaultProperties() {
        return defaultProperties;
    }

    /**
     * Set default properties
     * 
     * @param defaultProperties the defaultProperties to set
     */
    public static void setDefaultProperties(
            Map<String, String> defaultProperties) {
        DefaultGraphProperties.defaultProperties = defaultProperties;
    }
}