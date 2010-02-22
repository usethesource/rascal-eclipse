package org.rascalmpl.eclipse.library.viz;

import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.library.viz.JFBarChart;
import org.rascalmpl.library.viz.JFBoxPlot;
import org.rascalmpl.library.viz.JFHistogram;
import org.rascalmpl.library.viz.JFPieChart;
import org.rascalmpl.library.viz.JFXYChart;

public class JFChart {
	
	public JFChart(IValueFactory values){
		super();
	}
	
	public void barChart(IString label, IMap facts, IList settings) {
		JFChartViewer.open(JFBarChart.makeBarchart(label, facts, settings));
	}
	
	public void barChart(IString label, IList categories, IList facts, IList settings) {
		JFChartViewer.open(JFBarChart.makeBarchart(label, categories, facts, settings));
	}
	
	public void boxplot(IString label, IList facts, IList settings) {
		JFChartViewer.open(JFBoxPlot.makeBoxPlot(label, facts, settings));
	}
	
	public void histogram(IString label, IList facts, IInteger nbins, IList settings) {
		JFChartViewer.open(JFHistogram.makeHistogram(label, facts, nbins, settings));
	}
	
	public void pieChart(IString label, IMap facts, IList settings) {
		JFChartViewer.open(JFPieChart.makePiechart(label, facts, settings));
	}
	
	public void xyChart(IString label, IList facts, IList settings) {
		JFChartViewer.open(JFXYChart.makeXYChart(label, facts, settings));
	}
}
