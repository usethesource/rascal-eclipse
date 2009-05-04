/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.model;

import java.beans.PropertyChangeListener;

/**
 * The property publisher maintains a list of its listeners. These listeners are
 * notified when a change occurs in the publisher. By applying this design, a
 * loose coupling is established between the publisher and its listeners.
 * 
 * @see "http://en.wikipedia.org/wiki/Observer_pattern"
 * 
 * @author Jeroen van Schagen
 * @version $id
 */
public interface IPropertyPublisher {

    /**
     * Subscribe listener to the property publisher.
     * 
     * @param listener Reference to the added listener
     * @return True if the listener has been added, false otherwise.
     */
    boolean addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Fire property changed event to listeners.
     * 
     * @param propertyName Name of the property that changed
     * @param oldValue Previous property value
     * @param newValue New property value
     */
    void firePropertyChange(String key, Object oldValue, Object newValue);

    /**
     * Remove listener from property publisher.
     * 
     * @param listener Reference to the removed listener
     * @return True if the listener has been added, false otherwice.
     */
    boolean removePropertyChangeListener(PropertyChangeListener listener);

}