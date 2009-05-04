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
package org.dancingbear.graphbrowser.layout;

import java.util.ArrayList;

/**
 * A list containing nodes.
 * 
 * @author hudsonr
 * @since 2.1.2
 */
public class NodeList extends ArrayList<Node> {
	private static final long serialVersionUID = 5661135709426854643L;

	/**
     * Constructs an empty NodeList.
     */
    public NodeList() {
    	super();
    }

    /**
     * Constructs a NodeList with the elements from the specified list.
     * 
     * @param list the list whose elements are to be added to this list
     */
    public NodeList(NodeList list) {
        super(list);
    }

    void adjustRank(int delta) {
        if (delta == 0) {
            return;
        }
        for (Node node : this) {
            int rank = node.getRank();
            node.setRank(rank + delta);
        }
    }

    /**
     * Returns the Node at the given index.
     * 
     * @param index the index
     * @return the node at a given index
     */
    public Node getNode(int index) {
        return super.get(index);
    }

    public Node getNodeById(int identifier) {
        for (Node node : this) {
            if (node.getId() == identifier) {
                return node;
            } else if (node instanceof Subgraph) {
                Node subnode = ((Subgraph) node).getMembers().getNodeById(
                        identifier);
                if (subnode != null) {
                    return subnode;
                }
            }
        }

        return null;
    }

    void normalizeRanks() {
        int minRank = Integer.MAX_VALUE;
        for (Node node : this) {
            minRank = Math.min(minRank, node.getRank());
        }
        adjustRank(-minRank);
    }

    public void resetFlags() {
        for (Node node : this) {
            node.setFlag(false);
        }
    }

    void resetIndices() {
        for (Node node : this) {
            node.index = 0;
        }
    }

    void resetSortValues() {
        for (Node node : this) {
            node.setSortValue(0.0);
        }
    }

}
