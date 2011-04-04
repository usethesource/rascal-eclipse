/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Bert Lisser - Bert.Lisser@cwi.nl (CWI)
*******************************************************************************/
package org.rascalmpl.eclipse.box;

import java.net.URI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.rascalmpl.library.box.MakeBox;

public class ToHtml {

	static private Browser browser;
	static private Shell shell;
	static private String textToPrint;
	static private Display screen = Display.getCurrent() == null ? new Display()
			: Display.getCurrent();
	static final private MakeBox makeBox = new MakeBox();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (shell == null) {
			shell = new Shell(screen);
		}
		try {
			browser = new Browser(shell, SWT.MOZILLA);
					//| SWT.NO_REDRAW_RESIZE | SWT.H_SCROLL | SWT.V_SCROLL);
		} catch (SWTError e) {
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR
					| SWT.OK);
			messageBox.setMessage("Browser cannot be initialized."+e.getMessage());
			messageBox.setText("Exit");
			messageBox.open();
			System.exit(-1);
		}
		URI uri = BoxPrinter.getFileName();
		textToPrint = makeBox.toPrint("toHtml", uri);
		browser.setText(textToPrint);
		shell.open();
	}

}
