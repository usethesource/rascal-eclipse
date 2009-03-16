/**
 * 
 */
package org.meta_environment.rascal.eclipse.console;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.part.FileEditorInput;
import org.meta_environment.rascal.eclipse.Activator;

public class RascalErrorHyperLink implements IHyperlink {
	private final int offset;
	private final int len;
	private final IDocument doc;

	RascalErrorHyperLink(IDocument doc, int offset, int len) {
		this.doc = doc;
		this.offset = offset;
		this.len = len;
	}

	@Override
	public void linkActivated() {
		try {
			String match = doc.get(offset, len - 1);
			String[] filePosSplit = match.split(":");
			String file = filePosSplit[0];
			String[] lineColSplit = filePosSplit[1].split(",");
			int line = Integer.parseInt(lineColSplit[0]);
			int col = Integer.parseInt(lineColSplit[1]);
			openEditor(file, line, col);
		} catch (BadLocationException e) {
			Activator.getInstance().logException("hyperlink", e);
		}
	
	}

	@Override
	public void linkEntered() {
	}

	@Override
	public void linkExited() {
	}
	
	private void openEditor(String filename, int line, int col) {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

		if (win != null) {
			IWorkbenchPage page = win.getActivePage();

			if (page != null) {
				try {
					IFile file = new ProjectModuleLoader().getFile(filename);
					page.openEditor(new FileEditorInput(file),
							UniversalEditor.EDITOR_ID);
					int offset = doc.getLineOffset(line);
					ITextSelection select = new TextSelection(offset+ col, 1);
					page.getActiveEditor().getEditorSite().getSelectionProvider().setSelection(select);
				} catch (PartInitException e) {
					Activator.getInstance()
							.logException(
									"Could not open editor for: "
											+ filename, e);
				} catch (IOException e) {
					Activator.getInstance()
					.logException(
							"Could not open editor for: "
									+ filename, e);
				} catch (BadLocationException e) {
					Activator.getInstance()
					.logException(
							"Could not open editor for: "
									+ filename, e);
				}
			}
		}
	}
}