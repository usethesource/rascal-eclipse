/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.ui.parts.palette;

import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PanningSelectionToolEntry;

/**
 * Palette factory
 * 
 * By isolating palette initialization statements in a factory class. Editor
 * implementations will remain cleaner and easier to maintain.
 * 
 * @author Jeroen van Schagen
 * @date 13-02-2009
 */
public class PaletteFactory {

    /**
     * Create a palette root, containing editing and graph functionality.
     * 
     * @return root
     */
    public static PaletteRoot createPaletteRoot() {
        PaletteRoot root = new PaletteRoot();

        // Attach regular editor functionality
        PaletteDrawer manipulationGroup = new PaletteDrawer("Graph Browser");
        PanningSelectionToolEntry selectionEntry = new PanningSelectionToolEntry();
        manipulationGroup.add(selectionEntry);
        manipulationGroup.add(new MarqueeToolEntry());

        root.add(manipulationGroup);

        root.setDefaultEntry(selectionEntry);

        return root;
    }

}