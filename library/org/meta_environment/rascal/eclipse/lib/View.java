package org.meta_environment.rascal.eclipse.lib;

import org.eclipse.imp.pdb.facts.IMap;
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
	
	public static void barChart(IString label, IMap map, IValue settings) {
		ChartViewer.open(BarChart.makeBarchart(label, map, settings));
	}
	
	public static void pieChart(IString label, IMap map, IValue settings) {
		ChartViewer.open(PieChart.makePiechart(label, map, settings));
	}
	
	public static void xyChart(IString label, IMap map, IValue settings) {
		ChartViewer.open(XYChart.makeXYChart(label, map, settings));
	}
	
	public static void edit(IValue v) {
		org.eclipse.imp.pdb.ui.text.Editor.edit(v);
	}
}
