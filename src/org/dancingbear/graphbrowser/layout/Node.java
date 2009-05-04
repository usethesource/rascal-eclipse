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
package org.dancingbear.graphbrowser.layout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.CompoundDirectedGraphLayout;

/**
 * A node in a DirectedGraph. A node has 0 or more incoming and outgoing
 * {@link Edge}s. A node is given a width and height by the client. When a
 * layout places the node in the graph, it will determine the node's x and y
 * location. It may also modify the node's height.
 * 
 * 
 * 
 * @author Randy Hudson
 * @since 2.1.2
 */
public class Node {

    private int id;

    private Node left = null, right = null; // jkroon: initialized to null to
    // prevent uninitialized errors

    Object workingData[] = new Object[3];
    private int workingInts[] = new int[4];

    private Map<String, String> properties = null;

    /**
     * Clients may use this field to mark the Node with an arbitrary data
     * object.
     */
    private Object data;

    // used by various graph visitors
    private boolean flag;

    /**
     * The height of this node. This value should be set prior to laying out the
     * directed graph. Depending on the layout rules, a node's height may be
     * expanded to match the height of other nodes around it.
     */
    private int height = 40;

    /**
     * @deprecated use {@link #setRowConstraint(int)} and
     * {@link #getRowConstraint()}
     */
    private int rowOrder = -1;

    /**
     * The edges for which this node is the target.
     */
    private EdgeList incoming = new EdgeList();

    /**
     * The default attachment point for incoming edges. <code>-1</code>
     * indicates that the node's horizontal center should be used.
     */
    private int incomingOffset = -1;

    // A non-decreasing number given to consecutive nodes in a Rank.
    int index;

    // Used in Compound graphs to quickly determine whether a node is inside a
    // subgraph.
    int nestingIndex = -1;

    /**
     * The edges for which this node is the source.
     */
    private EdgeList outgoing = new EdgeList();

    Insets padding;
    private Subgraph parent;
    private int rank;

    /**
     * @deprecated for internal use only
     */
    private double sortValue;

    /**
     * The node's outgoing offset attachment point.
     */
    private int outgoingOffset = -1;

    /**
     * The node's width. The default value is 50.
     */
    private int width = 50;

    /**
     * The node's x coordinate.
     */
    private int x;
    /**
     * The node's y coordinate.
     */
    private int y;

    /**
     * Constructs a node with the given data object
     * 
     * @param data an arbitrary data object
     */
    public Node(Object data) {
        this(data, null);
    }

    /**
     * Constructs a node with the given data object and parent subgraph. This
     * node is added to the set of members for the parent subgraph
     * 
     * @param data an arbitrary data object
     * @param parent the parent subgraph or <code>null</code>
     */
    public Node(Object data, Subgraph parent) {
        this.data = data;
        this.parent = parent;
        if (parent != null)
            parent.addMember(this);
        this.rank = -1;

        properties = new HashMap<String, String>();

    }

    /**
     * Constructs a node inside the given subgraph.
     * 
     * @param parent the parent subgraph
     */
    public Node(Subgraph parent) {
        this(null, parent);

    }

    /**
     * Constructs a node not in a subgraph, without any data.
     * 
     */
    public Node() {
        this(null, null);
    }

    public int getId() {
        return id;
    }

    /**
     * Returns a reference to a node located left from this one
     * 
     * @return <code>Node</code> on the left from this one
     * @since 3.4
     */
    public Node getLeft() {
        return left;
    }

    /**
     * Returns the incoming attachment point. This is the distance from the left
     * edge to the default incoming attachment point for edges. Each incoming
     * edge may have it's own attachment setting which takes priority over this
     * default one.
     * 
     * @return the incoming offset
     */
    public int getOffsetIncoming() {
        if (incomingOffset == -1)
            return width / 2;
        return incomingOffset;
    }

    /**
     * Returns the outgoing attachment point. This is the distance from the left
     * edge to the default outgoing attachment point for edges. Each outgoing
     * edge may have it's own attachment setting which takes priority over this
     * default one.
     * 
     * @return the outgoing offset
     */
    public int getOffsetOutgoing() {
        if (outgoingOffset == -1)
            return width / 2;
        return outgoingOffset;
    }

    /**
     * Returns the padding for this node or <code>null</code> if the default
     * padding for the graph should be used.
     * 
     * @return the padding or <code>null</code>
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Returns the parent Subgraph or <code>null</code> if there is no parent.
     * Subgraphs are only for use in {@link CompoundDirectedGraphLayout}.
     * 
     * @return the parent or <code>null</code>
     */
    public Subgraph getParent() {
        return parent;
    }

    /**
     * Returns a reference to a node located right from this one
     * 
     * @return <code>Node</code> on the right from this one
     * @since 3.4
     */
    public Node getRight() {
        return right;
    }

    /**
     * Returns the row constraint for this node.
     * 
     * @return the row constraint
     * @since 3.2
     */
    public int getRowConstraint() {
        return getRowOrder();
    }

    public int[] getWorkingInts() {
        return workingInts;
    }

    public boolean isFlag() {
        return flag;
    }

    /**
     * For internal use only. Returns <code>true</code> if the given node is
     * equal to this node. This method is implemented for consitency with
     * Subgraph.
     * 
     * @param node the node in question
     * @return <code>true</code> if nested
     */
    boolean isNested(Node node) {
        return node == this;
    }

    /**
     * Check whether we have a virtual node
     * 
     * @return true if it's a virtual node
     */
    @SuppressWarnings("deprecation")
    public boolean isVirtualNode() {
        if (this instanceof VirtualNode) {
            return true;
        }

        return false;

    }

    Iterator<Node> iteratorNeighbors() {
        return new Iterator<Node>() {
            int offset;
            EdgeList list = outgoing;

            public boolean hasNext() {
                if (list == null)
                    return false;
                if (offset < list.size())
                    return true;
                if (list == outgoing) {
                    list = incoming;
                    offset = 0;
                }
                return offset < list.size();
            }

            public Node next() {
                Edge edge = list.getEdge(offset++);
                if (offset < list.size())
                    return edge.opposite(Node.this);
                if (list == outgoing) {
                    list = incoming;
                    offset = 0;
                } else
                    list = null;
                return edge.opposite(Node.this);
            }

            public void remove() {
                throw new RuntimeException("Remove not supported"); //$NON-NLS-1$
            }
        };
    }

    /**
     * Set the 'flag' (which is used by several visitors) for this node
     * 
     * @param flag the value of the flag to set
     */
    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the padding. <code>null</code> indicates that the default padding
     * should be used.
     * 
     * @param padding an insets or <code>null</code>
     */
    public void setPadding(Insets padding) {
        this.padding = padding;
    }

    /**
     * Sets the parent subgraph. This method should not be called directly. The
     * constructor will set the parent accordingly.
     * 
     * @param parent the parent
     */
    public void setParent(Subgraph parent) {
        this.parent = parent;
    }

    /**
     * Sets the row sorting constraint for this node. By default, a node's
     * constraint is <code>-1</code>. If two nodes have different values both >=
     * 0, the node with the smaller constraint will be placed to the left of the
     * other node. In all other cases no relative placement is guaranteed.
     * 
     * @param value the row constraint
     * @since 3.2
     */
    public void setRowConstraint(int value) {
        this.setRowOrder(value);
    }

    /**
     * Sets the size of this node to the given dimension.
     * 
     * @param size the new size
     * @since 3.2
     */
    public void setSize(Dimension size) {
        width = size.width;
        height = size.height;
    }

    public void setWorkingInts(int workingInts[]) {
        this.workingInts = workingInts;
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return "N(" + data + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        assert height < 0 : "Assertion failed: Height is <0, which is invalid";
        this.height = height;
    }

    /**
     * 
     * @return the outgoing edges for this node
     */
    public EdgeList getOutgoing() {
        return outgoing;
    }

    /**
     * 
     * @param outgoing the outgoing edges for this node
     */
    public void setOutgoing(EdgeList outgoing) {
        this.outgoing = outgoing;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {

        // System.out.println(this.data.toString() + "\t" + x);

        assert x < 0 : "Assertion failed: x is <0, which is invalid";
        this.x = x;
    }

    /**
     * adjust the X coord by some delta
     * 
     * @param delta the detla by which to adjust x
     */
    public void addToX(int delta) {
        x += delta;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        assert y < 0 : "Assertion failed: y is <0, which is invalid";
        this.y = y;
    }

    /**
     * adjust the Y coord by some delta
     * 
     * @param delta the delta by which to adjust y
     */
    public void addToY(int delta) {
        y += delta;
    }

    /**
     * 
     * @return the rank for this node
     */
    @SuppressWarnings("boxing")
    public int getRank() {
        if (getProperties().get("rankvalue") != null) {
            try {
                this.rank = Integer.valueOf(this.getProperties().get(
                        "rankvalue"));
            } catch (Exception e) {
                System.err.println("Invalid rank in hashmap for "
                        + this.toString());
                rank = -1;
            }
        }
        return this.rank;
    }

    /**
     * 
     * @param rank the rank for this node
     */
    public void setRank(int rank) {
        assert rank < 0 : "Rank " + rank + " is invalid";
        this.rank = rank;
        this.properties.put("rankvalue", rank + "");
    }

    public Map<String, String> getProperties() {

        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    /**
     * @param edge remove this specific edge from the edges outgoing for this
     * node
     */
    public void removeOutgoingEdge(Edge edge) {
        this.outgoing.remove(edge);
    }

    public EdgeList getIncoming() {
        return incoming;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setSortValue(double sortValue) {
        this.sortValue = sortValue;
    }

    public double getSortValue() {
        return sortValue;
    }

    public void setRowOrder(int rowOrder) {
        this.rowOrder = rowOrder;
    }

    public int getRowOrder() {
        return rowOrder;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public void setRight(Node right) {
        this.right = right;
    }

}
