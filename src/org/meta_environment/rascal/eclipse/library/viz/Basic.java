package org.meta_environment.rascal.eclipse.library.viz;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.ui.graph.Editor;

public class Basic {
	
	// Various views
	
	public static void graphView(IValue v) {
		Editor.open(v);
	}
	
	public static void textView(IValue v) {
		org.eclipse.imp.pdb.ui.text.Editor.edit(v);
	}
	
	public static void treeView(IValue v) {
		org.eclipse.imp.pdb.ui.tree.Editor.open(v);
	}
	
	// Various charts
	
	

}
