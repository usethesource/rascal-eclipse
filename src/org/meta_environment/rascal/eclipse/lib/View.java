package org.meta_environment.rascal.eclipse.lib;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.ui.graph.Editor;

public class View {
	public static void show(IValue v) {
		Editor.open(v);
	}
	
	public static void browse(IValue v) {
		Editor.open(v);
	}
}
 