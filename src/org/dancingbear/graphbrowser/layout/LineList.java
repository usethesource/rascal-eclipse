/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.layout;

import java.util.ArrayList;

/**
 * A class to create a list of lines.
 * 
 * @author Alex Hartog, Lars de Ridder
 * 
 */
public class LineList extends ArrayList<Line> {
	private static final long serialVersionUID = -3461974353894281521L;

	/**
     * Return a sublist of the LineList, toLine will NOT be included
     * 
     * @param fromLine the first line of the sublist
     * @param toLine the line AFTER the last line of the sublist
     */
    public LineList subList(int fromLine, int toLine) {
        LineList subList = new LineList();
        for (int lineIndex = fromLine; lineIndex < toLine; lineIndex++) {
            subList.add(this.get(lineIndex));
        }
        return subList;
    }
}
