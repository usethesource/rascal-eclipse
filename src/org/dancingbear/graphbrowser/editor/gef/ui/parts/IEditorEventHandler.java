/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.ui.parts;

import java.util.List;

import org.dancingbear.graphbrowser.controller.EditorController;

/**
 * Interface for Editor Event Handler
 * 
 * @author Menno Middel
 * 
 */

public interface IEditorEventHandler {

    /**
     * Get type of event
     * 
     * @return the name
     */
    public EventType getEventType();

    /**
     * fire event
     * 
     * @param values the new values
     */
    public void fireEvent(List<Object> values);

    /**
     * Get controller of event handler
     * 
     * @return the controller
     */
    public EditorController getController();

    /**
     * Set controller of event handler
     * 
     * @param controller the controller to set
     */
    public void setController(EditorController controller);
}
