package org.meta_environment.rascal.eclipse.library.viz;

import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IString;
import org.meta_environment.rascal.library.viz.BarChart;
import org.meta_environment.rascal.library.viz.BoxPlot;
import org.meta_environment.rascal.library.viz.Histogram;
import org.meta_environment.rascal.library.viz.PieChart;
import org.meta_environment.rascal.library.viz.XYChart;

public class Chart {
	
	public static void barChart(IString label, IMap facts, IList settings) {
		ChartViewer.open(BarChart.makeBarchart(label, facts, settings));
	}
	
	public static void barChart(IString label, IList categories, IList facts, IList settings) {
		ChartViewer.open(BarChart.makeBarchart(label, categories, facts, settings));
	}
	
	public static void boxplot(IString label, IList facts, IList settings) {
		ChartViewer.open(BoxPlot.makeBoxPlot(label, facts, settings));
	}
	
	public static void histogram(IString label, IList facts, IInteger nbins, IList settings) {
		ChartViewer.open(Histogram.makeHistogram(label, facts, nbins, settings));
	}
	
	public static void pieChart(IString label, IMap facts, IList settings) {
		ChartViewer.open(PieChart.makePiechart(label, facts, settings));
	}
	
	public static void xyChart(IString label, IList facts, IList settings) {
		ChartViewer.open(XYChart.makeXYChart(label, facts, settings));
	}
}
