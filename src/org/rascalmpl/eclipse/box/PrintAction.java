package org.rascalmpl.eclipse.box;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.rascalmpl.eclipse.Activator;

public class PrintAction implements IEditorActionDelegate {

	private FileEditorInput fi;

	public static void open(final IEditorInput v) {
		if (v == null) {
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
							page
									.openEditor(
											v,
											BoxViewer.EDITOR_ID,
											true,
											org.eclipse.ui.IWorkbenchPage.MATCH_ID
													| org.eclipse.ui.IWorkbenchPage.MATCH_INPUT);
						} catch (PartInitException e) {
							Activator.getInstance().logException(
									"failed to open boxviewer", e);
						}
						System.err.println("OK");
					}
				});
			}
		}
	}

	// public static final String actionId = "org.rascalmpl.eclipse.box.printAction";
	IFile file;

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection==null) return;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (ss.isEmpty()) return;
			Object element = ss.getFirstElement();
			if (element!=null && element instanceof IFile) {
				file = ((IFile) element);
			}
		} else {
			if (fi==null) return;
			file = fi.getFile();
		    }
	}

	public void run(IAction action) {
		// TODO Auto-generated method stub
		// URI uri = file.getLocationURI();
		System.err.println("RUN:" + file);
		open(new FileEditorInput(file));
		// BoxPrinter p = new BoxPrinter();
		// p.open(uri, null);
		// p.menuPrint();
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		// TODO Auto-generated method stub
		fi = (FileEditorInput) targetEditor.getEditorInput();
		System.err.println("activate:" + fi);

	}

}
