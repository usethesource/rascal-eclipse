/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.editparts;

import org.dancingbear.graphbrowser.controller.IGraphEditor;
import org.dancingbear.graphbrowser.editor.ui.views.properties.ContainerPropertySource;
import org.dancingbear.graphbrowser.model.IPropertyContainer;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * @author Jeroen van Schagen
 * @date 17-02-2009
 */
abstract class AbstractPropertyContainerEditPart<M extends IPropertyContainer>
        extends AbstractGraphicalEditPart implements
        IPropertyContainerEditPart<M> {

    private ContainerPropertySource propertySource;

    @Override
    public void activate() {
        getCastedModel().addPropertyChangeListener(this);
        super.activate();
    }

    @Override
    public void deactivate() {
        getCastedModel().removePropertyChangeListener(this);
        super.deactivate();
    }

    @SuppressWarnings("unchecked")
    public M getCastedModel() {
        return (M) getModel();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class key) {
        if (key == IPropertySource.class) {
            if (propertySource == null) {
                propertySource = new ContainerPropertySource(getCastedModel());
            }
            return propertySource;
        }

        return super.getAdapter(key);
    }

    /**
     * Gets the GraphEditor.
     * 
     * @return grapheditor
     */
    public IGraphEditor getGraphEditor() {

        EditDomain domain = this.getViewer().getEditDomain();

        if (domain instanceof DefaultEditDomain) {

            DefaultEditDomain defaultdomain = (DefaultEditDomain) domain;
            IGraphEditor graphEditor = (IGraphEditor) defaultdomain
                    .getEditorPart();

            return graphEditor;
        }

        return null;
    }
}