/*******************************************************************************
 * Copyright 2009, University of Amsterdam, Amsterdam, The Netherlands
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.dancingbear.graphbrowser.editor.gef.editparts;

import org.eclipse.gef.GraphicalEditPart;

/**
 * Highlightable connection interface. This interface can be used to indicate
 * that a ConnectionEditPart can be highlighted.
 * 
 * @author Jeroen Bach and Taco Witte
 */
public interface HighlightEditPart extends GraphicalEditPart {

    /**
     * Indicates that the connection is highlighted
     * 
     * @param value true is highlighted, false is not
     */
    void setHighlighted(boolean value);

    /**
     * Gets whether this connection is highlighted.
     * 
     * @return true is highlighted, false is not
     */
    boolean getHighlighted();
}
