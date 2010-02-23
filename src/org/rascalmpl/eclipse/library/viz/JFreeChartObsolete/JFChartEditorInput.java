package org.rascalmpl.eclipse.library.viz.JFreeChartObsolete;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.jfree.chart.JFreeChart;

public class JFChartEditorInput implements IEditorInput {
	private final JFreeChart chart;

	public JFChartEditorInput(JFreeChart chart) {
		this.chart = chart;
	}
	
	public boolean exists() {
		return chart != null;
	}

	public JFreeChart getChart() {
		return chart;
	}
	
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return chart.getTitle().getText();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return chart.getTitle().getText();
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

}
