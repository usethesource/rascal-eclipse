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
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.box;

import java.net.URI;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.rascalmpl.eclipse.editor.RascalEditor;
import org.rascalmpl.eclipse.library.vis.FigureViewer;

public class PrintHandler extends AbstractHandler {
	static final IThemeManager themeManager = PlatformUI.getWorkbench()
			.getThemeManager();
	static final ITheme currentTheme = themeManager.getCurrentTheme();
	static final FontRegistry fontRegistry = currentTheme.getFontRegistry();

	private void print(final Shell shell, final StyledText st, final Font printerFont) {
		StyledTextContent content = st.getContent();
		final StyledText nst = new StyledText(st.getParent(), SWT.READ_ONLY);
		nst.setContent(content);
		nst.setStyleRanges(st.getStyleRanges());
		nst.setFont(printerFont);
		nst.setLineSpacing(2);
		PrintDialog dialog = new PrintDialog(shell, SWT.PRIMARY_MODAL);
		final PrinterData data = dialog.open();
		if (data == null)
			return;
		final Printer printer = new Printer(data);
		nst.print(printer).run();
	}
	
	private void print(final Shell shell, FigureViewer fv) {
		PrintDialog dialog = new PrintDialog(shell, SWT.PRIMARY_MODAL);
		final PrinterData data = dialog.open();
		if (data == null)
			return;
		final Printer printer = new Printer(data);
		fv.print(printer);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Font printerFont = fontRegistry
				.get("rascal-eclipse.printerFontDefinition");
		if (HandlerUtil.getCurrentSelection(event) != null
				&& HandlerUtil.getCurrentSelectionChecked(event) instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) HandlerUtil
					.getCurrentSelectionChecked(event);
			if (sel.getFirstElement() instanceof IFile) {
				IFile f = (IFile) sel.getFirstElement();
				String ext = f.getFileExtension();
				URI uri = f.getLocationURI();
				IProject p = f.getProject();
				BoxPrinter boxPrinter = new BoxPrinter(p);
				boxPrinter.updateFont(printerFont);
				if (ext != null) {
					if (ext.equals("rsc"))
						boxPrinter.preparePrintRascal(uri);
					else
						boxPrinter.preparePrint(uri, ext);
					boxPrinter.menuPrint();
				}
				return null;
			}
		}
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		if (activeEditor != null) {
			Shell shell = activeEditor.getEditorSite().getShell();
			if (activeEditor instanceof BoxViewer) {
				BoxViewer ate = ((BoxViewer) activeEditor);
				print(shell, ate.getTextWidget(), printerFont);
				return null;
			}
			if (activeEditor instanceof RascalEditor) {
				RascalEditor ate = ((RascalEditor) activeEditor);
				print(shell, ate.getTextWidget(), printerFont);
				return null;

			}
			if (activeEditor instanceof FigureViewer) {
				FigureViewer ate = ((FigureViewer) activeEditor);
				print(shell, ate);
				return null;
			}
		}
		System.err.println("Wrong:" + activeEditor.getClass());
		return null;
	}
}
