/*************************************************************************
 * Copyright (c) 2009 University of Amsterdam, The Netherlands.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ************************************************************************/
package org.dancingbear.graphbrowser.layout.dot;

import org.dancingbear.graphbrowser.layout.model.DirectedGraph;
import org.dancingbear.graphbrowser.layout.model.Node;
import org.dancingbear.graphbrowser.layout.model.Subgraph;

public class SubgraphRankSolver extends GraphVisitor {
    public void visit(DirectedGraph g) {
        for (Node node : g.getNodes()) {
            if (node instanceof Subgraph) {
                ((Subgraph) node).fixInternalRanks(node.getRank());
            }
            if (node.getData() instanceof Subgraph) {
                if ("same".equals(((Node) node.getData()).getProperties().get(
                        "rank"))) {
                    ((Subgraph) node.getData())
                            .fixInternalRanks(node.getRank());
                }

            }
        }
    }
}
