package org.meta_environment.rascal.eclipse.library.viz;

import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.ui.graph.Editor;
import org.meta_environment.rascal.library.viz.BarChart;
import org.meta_environment.rascal.library.viz.BoxPlot;
import org.meta_environment.rascal.library.viz.Histogram;
import org.meta_environment.rascal.library.viz.PieChart;
import org.meta_environment.rascal.library.viz.XYChart;

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
