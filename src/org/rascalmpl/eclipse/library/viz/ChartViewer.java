package org.rascalmpl.eclipse.library.viz;

import org.eclipse.core.runtime.IProgressMonitor;
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
import org.rascalmpl.eclipse.Activator;

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
		
		
		if (input instanceof ChartEditorInput) {
			setSite(site);
			setInput(input);
		}
		else {
			throw new PartInitException("Input of chart visualization is not a chart");
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
		new ChartComposite(parent, SWT.NONE, ((ChartEditorInput) getEditorInput()).getChart(), true);
	}

	@Override
	public void setFocus() {
	}
	
	public static void open(final JFreeChart chart) {
		if (chart == null) {
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
							page.openEditor(new ChartEditorInput(chart), editorId);
						} catch (PartInitException e) {
							Activator.getInstance().logException("failed to open chart viewer", e);
						}
					}
				});
			}
		}
	}
}
