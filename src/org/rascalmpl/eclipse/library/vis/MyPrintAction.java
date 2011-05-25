package org.rascalmpl.eclipse.library.vis;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;

public class MyPrintAction extends Action {
	final private FigureViewer view;

	MyPrintAction(FigureViewer view) {
		this.view = view;
	}

	public void run() {
		PrintDialog dialog = new PrintDialog(view.getShell(), SWT.PRIMARY_MODAL);
		final PrinterData data = dialog.open();
		if (data == null)
			return;
		final Printer printer = new Printer(data);
		view.print(printer);
	}

}
