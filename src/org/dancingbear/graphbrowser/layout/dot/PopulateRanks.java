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
package org.dancingbear.graphbrowser.layout.dot;

import java.util.Stack;

import org.dancingbear.graphbrowser.layout.model.DirectedGraph;
import org.dancingbear.graphbrowser.layout.model.Edge;
import org.dancingbear.graphbrowser.layout.model.Node;

/**
 * This class takes a DirectedGraph with an optimal rank assignment and a
 * spanning tree, and populates the ranks of the DirectedGraph. Virtual nodes
 * are inserted for edges that span 1 or more ranks.
 * <P>
 * Ranks are populated using a pre-order depth-first traversal of the spanning
 * tree. For each node, all edges requiring virtual nodes are added to the
 * ranks.
 * 
 * @author Randy Hudson
 * @since 2.1.2
 */
class PopulateRanks extends GraphVisitor {

    private Stack<RevertableChange> changes = new Stack<RevertableChange>();

    /**
     * @see GraphVisitor#revisit(DirectedGraph)
     */
    public void revisit(DirectedGraph g) {

        for (int i = 0; i < changes.size(); i++) {
            RevertableChange change = changes.get(i);
            change.revert();
        }

    }

    /**
     * @see GraphVisitor#visit(DirectedGraph)
     */
    public void visit(DirectedGraph g) {
        if (g.getForestRoot() != null) {
            for (int i = g.getForestRoot().getOutgoing().size() - 1; i >= 0; i--)
                g.removeEdge(g.getForestRoot().getOutgoing().getEdge(i));
            g.removeNode(g.getForestRoot());
        }
        g.setRanks(new RankList());
        for (int i = 0; i < g.getNodes().size(); i++) {
            Node node = g.getNodes().getNode(i);
            g.getRanks().getRank(node.getRank()).add(node);
        }
        for (int i = 0; i < g.getNodes().size(); i++) {
            Node node = g.getNodes().getNode(i);
            for (int j = 0; j < node.getOutgoing().size();) {
                Edge e = node.getOutgoing().getEdge(j);
                if (e.getLength() > 1)
                    changes.push(new VirtualNodeCreation(e, g));
                else
                    j++;
            }
        }
    }

}
