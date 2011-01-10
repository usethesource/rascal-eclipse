package org.rascalmpl.eclipse.box;

import java.net.URI;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class PrintHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (HandlerUtil.getCurrentSelection(event) != null
				&& HandlerUtil.getCurrentSelectionChecked(event) instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) HandlerUtil
					.getCurrentSelectionChecked(event);
			if (sel.getFirstElement() instanceof IFile) {
				IFile f = (IFile) sel.getFirstElement();
				URI uri = f.getLocationURI();
				IProject p = f.getProject();
				BoxPrinter boxPrinter = new BoxPrinter(p);
				boxPrinter.preparePrint(uri);
				boxPrinter.menuPrint();
				return null;
			}
		}
		if (HandlerUtil.getActiveEditor(event) != null
				&& HandlerUtil.getActiveEditor(event) instanceof BoxViewer) {
			BoxViewer ate = ((BoxViewer) HandlerUtil
					.getActiveEditorChecked(event));
			ate.print();
			return null;
		}
		System.err.println("Wrong:" + this.getClass());
		return null;
	}
}
