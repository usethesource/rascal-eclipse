package org.meta_environment.rascal.eclipse.lib.View;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.ui.graph.Editor;

public class View {
	public static void viewGraph(IValue v) {
		Editor.open(v);
	}
	
	public static void browseGraph(IValue v) {
		
	}
}
