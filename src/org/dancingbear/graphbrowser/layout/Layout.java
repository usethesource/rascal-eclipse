package org.dancingbear.graphbrowser.layout;

import org.dancingbear.graphbrowser.layout.model.DirectedGraph;

public interface Layout {

	 /**
     * Lays out the given graph
     * 
     * @param graph the graph to layout
     */
    public void visit(DirectedGraph graph);
    
}
