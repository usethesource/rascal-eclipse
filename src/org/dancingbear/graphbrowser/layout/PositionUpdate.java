/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.layout;

public class PositionUpdate extends GraphVisitor {
    /**
     * @see GraphVisitor#visit(DirectedGraph)
     */
    public void visit(DirectedGraph g) {

        for (int r = 0; r < g.getRanks().size(); r++) {
            Rank rank = g.getRanks().getRank(r);
            Node prev = null, cur;
            for (int n = 0; n < rank.size(); n++) {
                cur = rank.getNode(n);
                cur.setLeft(prev);
                if (prev != null) {
                    prev.setRight(cur);
                }
                prev = cur;
            }
        }

    }
}
