package org.rascalmpl.eclipse.library.vis;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.rascalmpl.eclipse.box.BoxViewer;

public class MyPrintAction extends Action {
	final private EditorPart view;

	public MyPrintAction(EditorPart view) {
		this.view = view;
		this.setActionDefinitionId(ITextEditorActionConstants.PRINT);
	}

	public void run() {
		PrintDialog dialog = new PrintDialog(view.getSite().getShell(), SWT.PRIMARY_MODAL);
		final PrinterData data = dialog.open();
		if (data == null)
			return;
		final Printer printer = new Printer(data);
		if (view instanceof FigureViewer)
		((FigureViewer) view).print(printer);
		if (view instanceof BoxViewer)
			((BoxViewer) view).print(printer);	
	}

}
