package org.rascalmpl.eclipse.library.visdeprecated;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;

public class Basic {

	public Basic(IValueFactory values) {
		super();
	}

	// Various views

	public void textView(IValue v) {
		org.eclipse.imp.pdb.ui.text.Editor.edit(v);
	}

	public void treeView(IValue v) {
		org.eclipse.imp.pdb.ui.tree.Editor.open(v);
	}

	public void boxView(IValue v) {
		BoxViewer.display(v);
	}

}
