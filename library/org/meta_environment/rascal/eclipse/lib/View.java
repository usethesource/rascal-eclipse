package org.meta_environment.rascal.eclipse.lib;

import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.ui.graph.Editor;
import org.meta_environment.rascal.eclipse.lib.charts.ChartViewer;
import org.meta_environment.rascal.std.Chart.BarChart;
import org.meta_environment.rascal.std.Chart.PieChart;
import org.meta_environment.rascal.std.Chart.XYChart;

public class View {
	
	public static void show(IValue v) {
		Editor.open(v);
	}
	
	public static void barChart(IString label, IValue facts, IValue settings) {
		ChartViewer.open(BarChart.makeBarchart(label, facts, settings));
	}
	
	public static void pieChart(IString label, IValue facts, IValue settings) {
		ChartViewer.open(PieChart.makePiechart(label, facts, settings));
	}
	
	public static void xyChart(IString label, IValue facts, IValue settings) {
		ChartViewer.open(XYChart.makeXYChart(label, facts, settings));
	}
	
	public static void edit(IValue v) {
		org.eclipse.imp.pdb.ui.text.Editor.edit(v);
	}
	
	public static void browse(IValue v) {
		org.eclipse.imp.pdb.ui.tree.Editor.open(v);
	}
}
