/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.ui.input;

import org.dancingbear.graphbrowser.model.IModelGraph;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Input for grapheditor
 * 
 * @author Jeroen van Lieshout
 * @author Erik Slagter
 */
public class GraphEditorInput implements IEditorInput {

    private IModelGraph graph;

    public GraphEditorInput(IModelGraph graph) {
        this.graph = graph;
    }

    /**
     * Does the input exists
     * 
     * @return exists
     */
    public boolean exists() {
        return graph != null;
    }

    /**
     * Get the icon of this action
     * 
     * @return imageDescriptor Descriptor of icon
     */
    public ImageDescriptor getImageDescriptor() {
        return ImageDescriptor.createFromImage(Display.getDefault()
                .getSystemImage(SWT.ICON_ERROR));
    }

    /**
     * Get name of graph
     * 
     * @return name Name of graph
     */
    public String getName() {
        return graph.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    public IPersistableElement getPersistable() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Get ToolTipText from graph
     * 
     * @return name Name of graph as tooltip
     */
    public String getToolTipText() {
        return graph.getName();
    }

    /**
     * Get adapter object of class
     * 
     * @param adapter class
     * @return adapter
     */
    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (IModelGraph.class.equals(adapter)) {
            return graph;
        }

        return null;
    }

}
