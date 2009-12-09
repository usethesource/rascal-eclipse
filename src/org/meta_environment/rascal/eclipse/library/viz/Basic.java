package org.meta_environment.rascal.eclipse.library.viz;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.ui.graph.Editor;

public class Basic {
	
	public Basic(IValueFactory values){
		super();
	}
	
	// Various views
	
	public void graphView(IValue v) {
		Editor.open(v);
	}
	
	public void textView(IValue v) {
		org.eclipse.imp.pdb.ui.text.Editor.edit(v);
	}
	
	public void treeView(IValue v) {
		org.eclipse.imp.pdb.ui.tree.Editor.open(v);
	}
}
