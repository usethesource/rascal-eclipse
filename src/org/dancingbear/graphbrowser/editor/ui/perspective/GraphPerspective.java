/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.ui.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Graph browser perspective, gives a default perspective for graph browsing. In
 * this perspective all used views will be integrated directly.
 * 
 * @author Jeroen van Schagen
 * @date 11-03-2009
 */
public class GraphPerspective implements IPerspectiveFactory {

    public static final String ID = "org.dancingbear.graphbrowser.perspectives.browser";

    /**
     * see {@link IPerspectiveFactory#createInitialLayout(IPageLayout)}
     */
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true);
        layout.addStandaloneView(IPageLayout.ID_RES_NAV, true,
                IPageLayout.LEFT, 0.2f, editorArea);
        layout.addStandaloneView(IPageLayout.ID_PROP_SHEET, true,
                IPageLayout.RIGHT, 0.75f, editorArea);
    }

}