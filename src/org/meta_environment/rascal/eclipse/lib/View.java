package org.meta_environment.rascal.eclipse.lib;

import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.ui.graph.Editor;
import org.eclipse.imp.pdb.ui.graph.ValueEditorInput;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.meta_environment.rascal.eclipse.lib.charts.ChartViewer;

public class View {
	
	public static void show(IValue v) {
		Editor.open(v);
	}
	
	public static void chart(IString label, IMap map) {
		ChartViewer.open(label.getValue(), map);
	}
	
	public static void edit(final IValue v) {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		final IEditorInput input = new ValueEditorInput(v);

		if (win == null && wb.getWorkbenchWindowCount() != 0) {
			win = wb.getWorkbenchWindows()[0];
		}

		if (win != null) {
			final IWorkbenchPage page = win.getActivePage();
			if (page != null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							page.openEditor(input, "org.eclipse.ui.DefaultTextEditor");
						} catch (PartInitException e) {
							// TODO Auto-generated catch block
						}
					}
				});
			}
		}
	}
}
