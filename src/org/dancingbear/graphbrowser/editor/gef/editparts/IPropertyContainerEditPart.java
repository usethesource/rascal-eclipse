/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.editparts;

import java.beans.PropertyChangeListener;

import org.dancingbear.graphbrowser.controller.IGraphEditor;
import org.dancingbear.graphbrowser.model.IPropertyContainer;

interface IPropertyContainerEditPart<M extends IPropertyContainer> extends
        PropertyChangeListener {

    /**
     * Retrieve casted model.
     * 
     * @return model
     */
    public M getCastedModel();

    /**
     * Gets the GraphEditor.
     * 
     * @return grapheditor
     */
    public IGraphEditor getGraphEditor();

}