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
 * Class holds all default properties of an {@link IModelNode}.
 * 
 * @see "http://www.graphviz.org/pdf/dotguide.pdf"
 * 
 * @author Nickolas Heirbaut
 */
public final class DefaultNodeProperties {

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
        properties.put("color", "black");
        properties.put("comment", "");
        properties.put("distortion", "0.0");
        properties.put("fillcolor", "white");
        properties.put("fixedsize", "false");
        properties.put("fontcolor", "black");
        properties.put("fontname", "Times-Roman");
        properties.put("fontsize", "14");
        properties.put("group", "");
        properties.put("height", "50");
        properties.put("label", "");
        properties.put("layer", "");
        properties.put("linenumber", "1");
        properties.put("link", "");
        properties.put("orientation", "0.0");
        properties.put("peripheries", "shape-dependent");
        properties.put("regular", "false");
        properties.put("shape", "ellipse");
        properties.put("shapefile", "");
        properties.put("sides", "4");
        properties.put("skew", "0.0");
        properties.put("style", "");
        properties.put("url", "");
        properties.put("width", "75");
        properties.put("z", "0.0");
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
        DefaultNodeProperties.defaultProperties = defaultProperties;
    }
}