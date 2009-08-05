/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.dancingbear.graphbrowser.layout.model;

import java.util.ArrayList;

/**
 * A list of <code>Edge</code>s.
 * 
 * @author hudsonr
 * @since 2.1.2
 */
public class EdgeList extends ArrayList<Edge> {
	private static final long serialVersionUID = -1803831349264195053L;

	/**
     * Returns the edge for the given index.
     * 
     * @param index the index of the requested edge
     * @return the edge at the given index
     */
    public Edge getEdge(int index) {
        return super.get(index);
    }

    /**
     * For internal use only.
     * 
     * @return the minimum slack for this edge list
     */
    public int getSlack() {
        int slack = Integer.MAX_VALUE;
        for (Edge edge : this) {
            slack = Math.min(slack, edge.getSlack());
        }
        return slack;
    }

    /**
     * For intrenal use only.
     * 
     * @param i and index
     * @return a value
     */
    public int getSourceIndex(int i) {
        return getEdge(i).getSource().getIndex();
    }

    /**
     * For internal use only.
     * 
     * @param i an index
     * @return a value
     */
    public int getTargetIndex(int i) {
        return getEdge(i).getTarget().getIndex();
    }

    /**
     * For internal use only.
     * 
     * @return the total weight of all edges
     */
    public int getWeight() {
        int total = 0;
        for (Edge edge : this) {
            total += edge.getWeight();
        }
        return total;
    }

    /**
     * For internal use only
     * 
     * @return <code>true</code> if completely flagged
     */
    public boolean isCompletelyFlagged() {
        for (Edge edge : this) {
            if (!edge.isFlag()) {
                return false;
            }
        }
        return true;
    }

    /**
     * For internal use only. Resets all flags.
     * 
     * @param resetTree internal
     */
    public void resetFlags(boolean resetTree) {
        for (Edge edge : this) {
            edge.setFlag(false);
            if (resetTree) {
                edge.setTree(false);
            }
        }
    }

    /**
     * For internal use only.
     * 
     * @param value value
     */
    public void setFlags(boolean value) {
        for (Edge edge : this) {
            edge.setFlag(value);
        }
    }

}
