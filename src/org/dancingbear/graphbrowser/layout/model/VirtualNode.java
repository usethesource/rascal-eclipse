/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.dancingbear.graphbrowser.layout.model;

import org.eclipse.draw2d.geometry.Insets;

/**
 * @deprecated virtual nodes of an edge should be cast to Node.
 * @author Randy Hudson
 * @since 2.1.2
 */
public class VirtualNode extends Node {

    /**
     * The next node.
     */
    private Node next;

    /**
     * The previous node.
     */
    private Node prev;

    /**
     * Constructs a virtual node.
     * 
     * @deprecated This class is for internal use only.
     * @param e the edge
     * @param rank the row
     */
    public VirtualNode(Edge e, int rank) {
        super(e);
        getIncoming().add(e);
        getOutgoing().add(e);
        setWidth(e.getWidth());
        setHeight(0);
        setRank(rank);
        setPadding(new Insets(0, e.getPadding(), 0, e.getPadding()));
    }

    /**
     * Constructor.
     * 
     * @param o object
     * @param parent subgraph
     */
    public VirtualNode(Object o, Subgraph parent) {
        super(o, parent);
    }

    /**
     * Returns the index of {@link #prev}.
     * 
     * @return median
     */
    public double medianIncoming() {
        return getPrev().getIndex();
    }

    /**
     * Returns the index of {@link #next}.
     * 
     * @return outgoing
     */
    public double medianOutgoing() {
        return getNext().getIndex();
    }

    /**
     * For internal use only. Returns the original edge weight multiplied by the
     * omega value for the this node and the node on the previous rank.
     * 
     * @return the weighted weight, or omega
     */
    public int omega() {
        Edge e = (Edge) getData();
        if (e.getSource().getRank() + 1 < getRank()
                && getRank() < e.getTarget().getRank())
            return 8 * e.getWeight();
        return 2 * e.getWeight();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (getData() instanceof Edge)
            return "VN[" + (((Edge) getData()).getVNodes().indexOf(this) + 1) //$NON-NLS-1$
                    + "](" + getData() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ 
        return super.toString();
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getNext() {
        return next;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public Node getPrev() {
        return prev;
    }

}
