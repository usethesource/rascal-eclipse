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
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *******************************************************************************/
package org.rascalmpl.eclipse.library.vis;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.box.BoxPrinter;
import org.rascalmpl.eclipse.util.RascalInvoker;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.library.vis.FigureSWTApplet;
import org.rascalmpl.library.vis.IFigureApplet;

@SuppressWarnings("restriction")
public class FigureViewer extends EditorPart {

	protected static final String editorId = "rascal-eclipse.Figure.viewer";

	private IFigureApplet fpa;
	
	private ScrolledComposite sc;

	private IConstructor figure;

	private IPartListener2 partListener;

	public FigureViewer() {
		super();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}
	

	public void print(Printer printer) {
		if (printer.startJob("Figure")) {
			if (fpa!=null) fpa.print(printer);
			printer.endJob();
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		// System.err.println("QQQ:"+site.getId());
		if (input instanceof FigureEditorInput
				|| input instanceof FileEditorInput) {
			setInput(input);
		} else {
			throw new PartInitException(
					"Input of Figure visualization is not a Figure object");
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {

		// final int defaultWidth = 400;
		// final int defaultHeight = 400;

		final String title;
		sc = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL
				| SWT.V_SCROLL);
		// sc.setLayout(new FillLayout());
		sc.setAlwaysShowScrollBars(true);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		if (getEditorInput() instanceof FigureEditorInput) {
			FigureEditorInput f = (FigureEditorInput) getEditorInput();
			Canvas canvas = new Canvas(sc, SWT.NONE);
			// canvas.setBackground(FigureSWTApplet.getColor(SWT.COLOR_YELLOW));
			// canvas.setBackgroundMode(SWT.INHERIT_NONE);
			figure = f.getFig();
			fpa = new FigureSWTApplet(canvas, f.getIString().getValue(),
					figure, f.getCtx());
			sc.setContent(canvas);
			title = fpa.getName();
		} else if (getEditorInput() instanceof FileEditorInput) {
			FileEditorInput fi = (FileEditorInput) getEditorInput();
			IFile f = fi.getFile();
			String layout = this.getEditorSite().getId();
			int start = layout.lastIndexOf(".");
			layout = layout.substring(start + 1);
			URI uri = f.getLocationURI();
			IProject p = f.getProject();
			BoxPrinter boxPrinter = new BoxPrinter(p);
			figure = boxPrinter.getFigure(uri, layout);
			Canvas canvas = new Canvas(sc, SWT.NONE);
			fpa = new FigureSWTApplet(canvas, figure, null);
			sc.setContent(canvas);
			title = f.getName();
		} else
			return;
		sc.setMinSize(fpa.getFigureWidth(), fpa.getFigureHeight());
		sc.pack();
		this.setPartName(title);

		// Make sure that the frame gets the focus when the editor is brought to
		// the top
		partListener = new IPartListener2() {

			public void partActivated(IWorkbenchPartReference partRef) {
			}

			public void partBroughtToTop(IWorkbenchPartReference partRef) {
			}

			public void partClosed(IWorkbenchPartReference partRef) {
			}

			public void partDeactivated(IWorkbenchPartReference partRef) {
			}

			public void partHidden(IWorkbenchPartReference partRef) {
			}

			public void partVisible(IWorkbenchPartReference partRef) {
			}

			public void partOpened(IWorkbenchPartReference partRef) {
			}

			public void partInputChanged(IWorkbenchPartReference partRef) {
			}
		};

		getSite().getPage().addPartListener(partListener);

	}

	public void dispose() {
		IWorkbenchPage page = getSite().getPage();
		page.removePartListener(partListener);

		Workbench.getInstance().getEditorHistory().remove(getEditorInput());

		super.dispose();
	}

	@Override
	public void setFocus() {
	}

	public static void open(final IString name, final IConstructor fig,
			final IEvaluatorContext ctx) {

		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();

		if (win == null && wb.getWorkbenchWindowCount() != 0) {
			win = wb.getWorkbenchWindows()[0];
		}

		if (win != null) {
			final IWorkbenchPage page = win.getActivePage();

			if (page != null) {
				RascalInvoker.invokeAsync(new Runnable() {
					public void run() {
						try {
							page.openEditor(new FigureEditorInput(name, fig, ctx), editorId);
						} catch (PartInitException e) {
							Activator.getInstance().logException("failed to open Figure viewer", e);
						}
					}
				}, ctx.getEvaluator());
			}
		}
	}
}
