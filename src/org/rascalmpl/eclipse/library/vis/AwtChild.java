/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:

 *   * Bert Lisser - Bert.Lisser@cwi.nl (CWI)
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
*******************************************************************************/

package org.rascalmpl.eclipse.library.vis;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.rascalmpl.library.vis.FigurePApplet;

public class AwtChild extends Composite {

	final FigurePApplet figurePApp;

	final Frame frame;

	public Frame getFrame() {
		return frame;
	}

	public FigurePApplet getFigurePApp() {
		return figurePApp;
	}

	AwtChild(final Composite parent, final FigurePApplet figurePApp) {
		super(parent, SWT.DOUBLE_BUFFERED | SWT.EMBEDDED);
		this.setLayout(new FillLayout());
		this.figurePApp = figurePApp;
		frame = SWT_AWT.new_Frame(this);
		frame.setLocation(0, 0);
		frame.add(figurePApp);
		figurePApp.init(); // Initialize the FigurePApplet
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				figurePApp.destroy();
			}
		});
		// An extra panel (a hack that is needed on older JDKs)

		@SuppressWarnings("serial")
		final Panel panel = new Panel(new BorderLayout()) {
			@Override
			public void update(java.awt.Graphics g) {
				/* Do not erase the background */
				paint(g);
			}
		};
		frame.add(panel);
		frame.setVisible(true);
		frame.pack();
	}

}
