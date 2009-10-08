package org.eclipse.imp.pdb.ui.text;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.ui.PDBUIPlugin;
import org.eclipse.imp.pdb.ui.ValueEditorInput;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class Editor {
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
							// TODO: log exception somewhere
						}
					}
				});
			}
		}
	}
}
