package org.rascalmpl.eclipse.library.util;

import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;

public class ValueUI {
	public ValueUI(IValueFactory vf) { }

	public void text(IValue v, IInteger tabsize) {
		org.eclipse.imp.pdb.ui.text.Editor.edit(v, true, tabsize.intValue());
	}

	public void tree(IValue v) {
		org.eclipse.imp.pdb.ui.tree.Editor.open(v);
	}
}
