package org.meta_environment.rascal.eclipse.lib.charts;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.ui.graph.ValueEditorInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.meta_environment.ValueFactoryFactory;
import org.meta_environment.rascal.std.Chart.PieChart;

public class ChartViewer extends EditorPart {
	protected static final String editorId = "rascal-eclipse.charts.viewer";

	public ChartViewer() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		
		
		if (input instanceof ValueEditorInput) {
			setSite(site);
			setInput(input);
			
			if (!((ValueEditorInput) input).getValue().getType().isMapType()) {
				throw new PartInitException("Input is not a map");
			}
		}
		else {
			throw new PartInitException("Input of chart visualization is not a value");
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		ValueEditorInput input = (ValueEditorInput) getEditorInput();
		IString name = ValueFactoryFactory.getValueFactory().string(input.getName());
		JFreeChart chart = PieChart.makePiechart(name, (IMap) input.getValue());
		new ChartComposite(parent, SWT.NONE, chart, true);
	}

	@Override
	public void setFocus() {
	}
	
	public static void open(final String label, final IMap value) {
		if (value == null) {
			return;
		}
	 	IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

		if (win == null && wb.getWorkbenchWindowCount() != 0) {
			win = wb.getWorkbenchWindows()[0];
		}
		
		if (win != null) {
			final IWorkbenchPage page = win.getActivePage();

			if (page != null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							page.openEditor(new ValueEditorInput(label, value), editorId);
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
						}
					}
				});
			}
		}
	}
}
