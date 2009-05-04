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

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * Sorts Ranks during the up and down sweeps of the MinCross visitor.
 * 
 * @author Randy Hudson
 * @since 2.1.2
 */
class RankSorter {

    Random flipflop = new Random(3);
    Node node;
    double rankSize, prevRankSize, nextRankSize;
    int currentRow;
    Rank rank;
    double progress;
    DirectedGraph g;

    private void assignIncomingSortValues() {
        rankSize = rank.total;
        prevRankSize = g.getRanks().getRank(currentRow - 1).total;
        if (currentRow < g.getRanks().size() - 1)
            nextRankSize = g.getRanks().getRank(currentRow + 1).total;
        for (int n = 0; n < rank.count(); n++) {
            node = rank.getNode(n);
            sortValueIncoming();
        }
    }

    private void assignOutgoingSortValues() {
        rankSize = rank.total;
        prevRankSize = g.getRanks().getRank(currentRow + 1).total;
        if (currentRow > 1)
            nextRankSize = g.getRanks().getRank(currentRow - 1).total;

        for (int n = 0; n < rank.count(); n++) {
            node = rank.getNode(n);
            sortValueOutgoing();
        }
    }

    double evaluateNodeIncoming() {
        boolean change = false;
        EdgeList incoming = node.getIncoming();
        do {
            change = false;
            for (int i = 0; i < incoming.size() - 1; i++) {
                if (incoming.getSourceIndex(i) > incoming.getSourceIndex(i + 1)) {
                    Edge e = incoming.getEdge(i);
                    incoming.set(i, incoming.get(i + 1));
                    incoming.set(i + 1, e);
                    change = true;
                }
            }
        } while (change);

        int n = incoming.size();
        if (n == 0) {
            return node.index * prevRankSize / rankSize;
        }
        if (n % 2 == 1)
            return incoming.getSourceIndex(n / 2);

        int l = incoming.getSourceIndex(n / 2 - 1);
        int r = incoming.getSourceIndex(n / 2);
        if (progress >= 0.8 && n > 2) {
            int dl = l - incoming.getSourceIndex(0);
            int dr = incoming.getSourceIndex(n - 1) - r;
            if (dl < dr)
                return l;
            if (dl > dr)
                return r;
        }
        if (progress > 0.25 && progress < 0.75) {
            if (flipflop.nextBoolean())
                return (l + l + r) / 3.0;
            return (r + r + l) / 3.0;
        }
        return (l + r) / 2.0;
    }

    double evaluateNodeOutgoing() {
        boolean change = false;
        EdgeList outgoing = node.getOutgoing();
        do {
            change = false;
            for (int i = 0; i < outgoing.size() - 1; i++) {
                if (outgoing.getTargetIndex(i) > outgoing.getTargetIndex(i + 1)) {
                    Edge e = outgoing.getEdge(i);
                    outgoing.set(i, outgoing.get(i + 1));
                    outgoing.set(i + 1, e);
                    change = true;
                }
            }
        } while (change);

        int n = outgoing.size();
        if (n == 0)
            return node.index * prevRankSize / rankSize;
        if (n % 2 == 1)
            return outgoing.getTargetIndex(n / 2);
        int l = outgoing.getTargetIndex(n / 2 - 1);
        int r = outgoing.getTargetIndex(n / 2);
        if (progress >= 0.8 && n > 2) {
            int dl = l - outgoing.getTargetIndex(0);
            int dr = outgoing.getTargetIndex(n - 1) - r;
            if (dl < dr)
                return l;
            if (dl > dr)
                return r;
        }
        if (progress > 0.25 && progress < 0.75) {
            if (flipflop.nextBoolean())
                return (l + l + r) / 3.0;
            return (r + r + l) / 3.0;
        }
        return (l + r) / 2.0;
    }

    public void init(DirectedGraph g) {
        this.g = g;
        for (int i = 0; i < g.getRanks().size(); i++) {
            rank = g.getRanks().getRank(i);

            // Sort the ranks based on their constraints. Constraints are
            // preserved throughout.
            Collections.sort(rank, new Comparator() {
                public int compare(Object left, Object right) {
                    return ((Node) left).getRowOrder()
                            - ((Node) right).getRowOrder();
                }
            });
            postSort();
        }
    }

    void optimize(DirectedGraph g) {
    }

    private void postSort() {
        rank.assignIndices();
    }

    void sort() {
        boolean change;
        do {
            change = false;
            for (int i = 0; i < rank.size() - 1; i++)
                change |= swap(i);
            if (!change)
                break;
            change = false;
            for (int i = rank.size() - 2; i >= 0; i--)
                change |= swap(i);
        } while (change);
    }

    public void sortRankIncoming(DirectedGraph g, Rank rank, int row,
            double progress) {
        this.currentRow = row;
        this.rank = rank;
        this.progress = progress;
        assignIncomingSortValues();
        sort();
        postSort();
    }

    public void sortRankOutgoing(DirectedGraph g, Rank rank, int row,
            double progress) {
        this.currentRow = row;
        this.rank = rank;
        this.progress = progress;
        assignOutgoingSortValues();
        sort();
        postSort();
    }

    void sortValueIncoming() {
        node.setSortValue(evaluateNodeIncoming());
        // $TODO restore this optimization
        // if (progress == 0.0 && !(node instanceof VirtualNode))
        // node.sortValue = -1;
        double value = evaluateNodeOutgoing();
        if (value < 0)
            value = node.index * nextRankSize / rankSize;
        node.setSortValue(node.getSortValue() + (value * progress));
        // if (progress < 0.7 && node.sortValue != -1)
        // node.sortValue += Math.random() * rankSize / (5 + 8 * progress);
    }

    void sortValueOutgoing() {
        node.setSortValue(evaluateNodeOutgoing());
        // $TODO restore this optimization
        // if (progress == 0.0 && !(node instanceof VirtualNode))
        // node.sortValue = -1;
        double value = evaluateNodeIncoming();
        if (value < 0)
            value = node.index * nextRankSize / rankSize;
        node.setSortValue(node.getSortValue() + (value * progress));
        // if (progress < 0.7 && node.sortValue != -1)
        // node.sortValue += Math.random() * rankSize / (5 + 8 * progress);
    }

    boolean swap(int i) {
        Node left = rank.getNode(i);
        Node right = rank.getNode(i + 1);
        if (GraphUtilities.isConstrained(left, right))
            return false;
        if (left.getSortValue() <= right.getSortValue())
            return false;
        rank.set(i, right);
        rank.set(i + 1, left);
        return true;
    }

}
