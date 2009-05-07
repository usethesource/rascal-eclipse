/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.controller.events;

import java.io.IOException;
import java.util.List;

import org.dancingbear.graphbrowser.controller.EditorController;
import org.dancingbear.graphbrowser.editor.gef.ui.parts.EventType;
import org.dancingbear.graphbrowser.editor.gef.ui.parts.IEditorEventHandler;
import org.dancingbear.graphbrowser.exporter.ExportException;
import org.dancingbear.graphbrowser.model.IModelGraph;

/**
 * Event Handler for Save functionality
 * 
 * @author Menno Middel
 * 
 */
public class SaveEventHandler implements IEditorEventHandler {

    private EditorController controller;
    
    public SaveEventHandler(EditorController controller){
    	super();
    	
    	this.controller = controller;
    }

    /**
     * Fire Save event
     * 
     * @param values List of values for Save event
     */
    public void fireEvent(List<Object> values) {
        IModelGraph graph = null;
        String fileName = null;
        for (Object value : values) {

            if (value instanceof String) {
                fileName = (String) value;
            }
            if (value instanceof IModelGraph) {
                graph = (IModelGraph) value;
            }

        }
        if (graph == null) {
            return;
        }
        if (fileName == null) {
            return;
        }

        try {
            this.controller.saveDotFile(graph, fileName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExportException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Get type of event
     * 
     * @return type
     */
    public EventType getEventType() {
        return EventType.Save;
    }

    /**
     * Get controller of Save Event Handler
     * 
     * @return the controller
     */
    public EditorController getController() {
        return controller;
    }

    /**
     * Set controllor for this Save Event Handler
     * 
     * @param controller the controller to set
     */
    public void setController(EditorController controller) {
        this.controller = controller;
    }

}
