/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Bert Lisser - Bert.Lisser@cwi.nl (CWI)
*******************************************************************************/
package org.rascalmpl.eclipse.box;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class Box {
	enum TAG {
		VR(SWT.ITALIC, blackColor), NM(SWT.NORMAL, darkBlueColor), KW(SWT.BOLD,
				darkMagentaColor), DF(SWT.NORMAL, blackColor), SG(SWT.ITALIC,
				darkBlueColor), CT(SWT.ITALIC, blackColor), SC(SWT.ITALIC,
				darkGreenColor);
		Font displayFont, printerFont;
		Color color;
		int style;

		TAG(int stylePar, Color parColor) {
			style = stylePar;
			displayFont = new Font(Display.getCurrent(), new FontData(
					"monospace", 10, style));
			printerFont = new Font(Display.getCurrent(), new FontData(
					"monospace", 6, style));
			color = parColor;
		}
	}

	private static void updateFont(String fontName, int size) {
		for (TAG t : Box.TAG.values()) {
			t.printerFont = new Font(Display.getCurrent(), new FontData(
					fontName, size, t.printerFont.getFontData()[0].getStyle()));
		}

	}
	
	static void updateFont(Font f) {
		if (f.getFontData().length==0) return;
		FontData d = f.getFontData()[0];
		updateFont(d.getName(),d.getHeight());
	}

	static Color darkMagentaColor = new Color(Display.getCurrent(), new RGB(
			127, 0, 85));
	static Color blackColor = getColor(SWT.COLOR_BLACK);
	static Color blueColor = getColor(SWT.COLOR_BLUE);
	static Color grayColor = getColor(SWT.COLOR_GRAY);
	static Color yellowColor = getColor(SWT.COLOR_YELLOW);
	static Color magentaColor = getColor(SWT.COLOR_MAGENTA);
	static Color darkGreenColor = getColor(SWT.COLOR_DARK_GREEN);
	static Color whiteColor = getColor(SWT.COLOR_WHITE);
	static Color darkBlueColor = new Color(Display.getCurrent(), new RGB(0, 0,
			192));

	private static Color getColor(final int which) {
		Display display = Display.getCurrent();
		if (display != null)
			return display.getSystemColor(which);
		display = Display.getDefault();
		final Color result[] = new Color[1];
		display.syncExec(new Runnable() {
			public void run() {
				synchronized (result) {
					result[0] = Display.getCurrent().getSystemColor(which);
				}
			}
		});
		synchronized (result) {
			return result[0];
		}
	}

}
