package org.meta_environment.rascal.eclipse.lib;

import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.ui.graph.Editor;
import org.meta_environment.rascal.eclipse.lib.charts.ChartViewer;
import org.meta_environment.rascal.std.Chart.BarChart;
import org.meta_environment.rascal.std.Chart.BoxPlot;
import org.meta_environment.rascal.std.Chart.Histogram;
import org.meta_environment.rascal.std.Chart.PieChart;
import org.meta_environment.rascal.std.Chart.XYChart;

public class View {
	
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
	
	// barchart
	
	public static void barChart(IString label, IMap facts, IValue settings) {
		ChartViewer.open(BarChart.makeBarchart(label, facts, settings));
	}
	
	public static void barChart(IString label, IList categories, IList facts, IValue settings) {
		ChartViewer.open(BarChart.makeBarchart(label, categories, facts, settings));
	}
	
	// boxplot
	
	public static void boxplot(IString label, IList facts, IValue settings) {
		ChartViewer.open(BoxPlot.makeBoxPlot(label, facts, settings));
	}
	
	// histogram
	
	public static void histogram(IString label, IList facts, IInteger nbins, IValue settings) {
		ChartViewer.open(Histogram.makeHistogram(label, facts, nbins, settings));
	}
	
	// piechart
	
	public static void pieChart(IString label, IMap facts, IValue settings) {
		ChartViewer.open(PieChart.makePiechart(label, facts, settings));
	}
	
	// xychart
	
	public static void xyChart(IString label, IList facts, IValue settings) {
		ChartViewer.open(XYChart.makeXYChart(label, facts, settings));
	}

}
