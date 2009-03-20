/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.ui.views.properties;

import java.util.ArrayList;

import org.dancingbear.graphbrowser.model.IPropertyContainer;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Connects a IPropertyContainer model with the property view in Eclipse. This
 * class allows the retrieval and manipulation of property values.
 * 
 * @author jeroenvs
 * @date 28-02-2009
 */
public class ContainerPropertySource implements IPropertySource {

    private final IPropertyContainer container;

    /**
     * Construct property source with container model.
     * 
     * @param container Model
     */
    public ContainerPropertySource(IPropertyContainer container) {
        this.container = container;
    }

    /**
     * Retrieve edit value.
     */
    public Object getEditableValue() {
        // Redundant
        return null;
    }

    /**
     * Retrieve property descriptors.
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        ArrayList<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();

        for (String key : container.getPropertyKeys()) {
            // TODO: Convert to more specific property descriptors
            properties.add(new TextPropertyDescriptor(key, key));
        }

        return properties.toArray(new IPropertyDescriptor[0]);
    }

    /**
     * Retrieve the value of a certain property.
     */
    public Object getPropertyValue(Object id) {
        return container.getProperty(id.toString());
    }

    /**
     * Check if property is contained.
     */
    public boolean isPropertySet(Object id) {
        return container.getPropertyKeys().contains(id);
    }

    /**
     * Reset property value.
     */
    public void resetPropertyValue(Object id) {
        String defaultValue = container.getDefaultProperty(id.toString());

        if (defaultValue != null) {
            container.setProperty(id.toString(), defaultValue);
        }
    }

    /**
     * Change property value.
     */
    public void setPropertyValue(Object id, Object value) {
        container.setProperty(id.toString(), value.toString());
    }

    /**
     * Retrieve property container model.
     * 
     * @return Model
     */
    public IPropertyContainer getContainer() {
        return container;
    }

}