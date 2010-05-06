package org.rascalmpl.eclipse.box;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.rascalmpl.library.box.BoxPrinter;

public class PrintAction implements IActionDelegate {

	// public static final String actionId = "org.rascalmpl.eclipse.box.printaction";
	IFile file;
	
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object element = ss.getFirstElement();
			// System.err
			// .println("Selected:" + element + " " + element.getClass());
			if (element instanceof IFile) {
				file = ((IFile) element);
			}
		}

	}
	
	public void run(IAction action) {
		// TODO Auto-generated method stub
		URI uri = file.getLocationURI();
		BoxPrinter p = new BoxPrinter();
		p.open(uri, null);
		p.menuPrint();
	}
}
