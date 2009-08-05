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
package org.dancingbear.graphbrowser.layout.dot;

import org.dancingbear.graphbrowser.layout.model.DirectedGraph;

/**
 * Sweeps up and down the ranks rearranging them so as to reduce edge crossings.
 * 
 * @author Randy Hudson
 * @since 2.1.2
 */
class MinCross extends GraphVisitor {

    static final int MAX = 45;

    private DirectedGraph g;
    private RankSorter sorter = new RankSorter();

    public MinCross() {
    	super();
    }

    /**
     * @since 3.1
     */
    public MinCross(RankSorter sorter) {
        setRankSorter(sorter);
    }

    /**
     * Set a ranksorter
     * 
     * @param sorter, the sorting implementation
     */
    public void setRankSorter(RankSorter sorter) {
        this.sorter = sorter;
    }

    void solve() {
        Rank rank;
        for (int loop = 0; loop < MAX; loop++) {
            for (int row = 1; row < g.getRanks().size(); row++) {
                rank = g.getRanks().getRank(row);
                sorter.sortRankIncoming(g, rank, row, (double) loop / MAX);
            }
            if (loop == MAX - 1)
                continue;
            for (int row = g.getRanks().size() - 2; row >= 0; row--) {
                rank = g.getRanks().getRank(row);
                sorter.sortRankOutgoing(g, rank, row, (double) loop / MAX);
            }
        }
    }

    /**
     * @param g The graph to visit
     */
    public void visit(DirectedGraph g) {
        sorter.init(g);
        this.g = g;
        solve();
        sorter.optimize(g);
    }

}
