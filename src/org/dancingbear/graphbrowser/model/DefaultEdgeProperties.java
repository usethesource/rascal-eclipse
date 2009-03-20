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
 * Class holds all default properties of an {@link IModelEdge}.
 * 
 * @see "http://www.graphviz.org/pdf/dotguide.pdf"
 * 
 * @author Nickolas Heirbaut
 */
public final class DefaultEdgeProperties {

    /**
     * Property container
     */
    private static Map<String, String> defaultProperties = initializeProperties();

    /**
     * Initialize properties
     * 
     * @return Map with properties
     */
    private static Map<String, String> initializeProperties() {
        Map<String, String> properties = new Hashtable<String, String>();
        properties.put("arrowhead", "normal");
        properties.put("arrowsize", "1.0");
        properties.put("arrowtail", "normal");
        properties.put("color", "black");
        properties.put("comment", "");
        properties.put("contraint", "true");
        properties.put("decorate", "");
        properties.put("dir", "forward");
        properties.put("fontcolor", "black");
        properties.put("fontname", "Times-Roman");
        properties.put("fontsize", "14");
        properties.put("headlabel", "");
        properties.put("headport", "");
        properties.put("headURL", "");
        properties.put("label", "");
        properties.put("labelangle", "-25.0");
        properties.put("labeldistance", "1.0");
        properties.put("labelfloat", "false");
        properties.put("labelfontcolor", "black");
        properties.put("labelfontname", "Times-Roman");
        properties.put("labelfontsize", "14");
        properties.put("layer", "");
        properties.put("lhead", "");
        properties.put("ltail", "");
        properties.put("minlen", "1");
        properties.put("samehead", "");
        properties.put("style", "");
        properties.put("taillabel", "");
        properties.put("tailport", "");
        properties.put("tailURL", "");
        properties.put("weight", "1");
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
        DefaultEdgeProperties.defaultProperties = defaultProperties;
    }
}