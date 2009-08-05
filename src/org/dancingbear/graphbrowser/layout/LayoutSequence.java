package org.dancingbear.graphbrowser.layout;

import java.util.Arrays;
import java.util.List;

import org.dancingbear.graphbrowser.layout.model.DirectedGraph;

public class LayoutSequence implements Layout {

	List<Layout> layouts;

	public LayoutSequence(Layout... l) {
		layouts = Arrays.asList(l);
	}

	public void visit(DirectedGraph graph) {
		for(Layout l: layouts) {
			l.visit(graph);
		}
	}

}
