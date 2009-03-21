/**
 * 
 */
package org.meta_environment.rascal.eclipse.console;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.part.FileEditorInput;
import org.meta_environment.rascal.eclipse.Activator;

public class RascalErrorHyperLink implements IHyperlink {
	private final String filename;
	private final int line;
	private final int col;

	RascalErrorHyperLink(String filename, int line, int col) {
		this.line = line;
		this.col = col;
		this.filename = filename;
	}

	@Override
	public void linkActivated() {
		try {
			IFile file = new ProjectModuleLoader().getFile(filename);
			openEditor(file, line, col);
		} catch (IOException e) {
			Activator.getInstance().logException("hyperlink", e);
		} catch (CoreException e) {
			Activator.getInstance().logException("hyperlink", e);
		}
	}

	@Override
	public void linkEntered() {
	}

	@Override
	public void linkExited() {
	}
	
	private void openEditor(IFile file, int line, int col) {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

		if (win != null) {
			IWorkbenchPage page = win.getActivePage();

			if (page != null) {
				try {
					page.openEditor(new FileEditorInput(file),
							UniversalEditor.EDITOR_ID);
				} catch (PartInitException e) {
					Activator.getInstance()
							.logException(
									"Could not open editor for: "
											+ filename, e);
				}
			}
		}
	}
}