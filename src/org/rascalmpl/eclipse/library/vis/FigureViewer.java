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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.EditorInputTransfer.EditorInputData;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.util.RascalInvoker;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.library.vis.swt.FigureExecutionEnvironment;

@SuppressWarnings("restriction")
public class FigureViewer extends EditorPart {

	protected static final String editorId = "rascal-eclipse.Figure.viewer";

	private FigureExecutionEnvironment fpa;

	// private IPartListener2 partListener;

	public FigureViewer() {
		super();
	}
	
	public void createPartControl(Composite parent) {
		FigureEditorInput f = (FigureEditorInput) getEditorInput();
		IConstructor cfig = (IConstructor)f.getFig();
		fpa = new FigureExecutionEnvironment(parent, cfig, f.getCtx());
		this.setPartName(f.getName());
	}
	
	
	public void doSave(IProgressMonitor monitor) {}
	
	public void doSaveAs() {}
	
	public void print(Printer printer) {
		/*if (printer.startJob("Figure")) {
			printer.endJob();
		}*/
	}
	
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		if (input instanceof FigureEditorInput){
		//		|| input instanceof FileEditorInput) {
			setInput(input);
		} else {
			throw new PartInitException(
					"Input of Figure visualization is not a Figure object");
		}
	}
	
	public boolean isDirty() {
		return false;
	}
	
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public Shell getShell() {
		return fpa.getRootApplet().getShell();
	}
	


	public void dispose() {
		if(fpa != null) fpa.dispose();
		Workbench.getInstance().getEditorHistory().remove(getEditorInput());
		super.dispose();
	}
	
	public void setFocus() {
		//if(fpa.getRootApplet() != null && !fpa.getRootApplet().isDisposed()){
			//fpa.getRootApplet().setFocus();
		//}
	}

	public static void open(final IString name, final IValue fig,
			final IEvaluatorContext ctx) {

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
							IEditorInput p = new FigureEditorInput(name, fig, ctx);
							IEditorPart e = page.findEditor(p);
							if(e != null){
								page.closeEditor(e, false);
							} 
							page.openEditor(p, editorId);
							
						} catch (PartInitException e) {
							Activator.getInstance().logException("failed to open Figure viewer", e);
						}
					}
				});
			}
		}
	}
	
	
	/*
	public void createPartControl(Composite parent) {
		// final int defaultWidth = 400;
		// final int defaultHeight = 400;
		final String title;
		// sc.setLayout(new FillLayout());
		if (getEditorInput() instanceof FigureEditorInput) {
			FigureEditorInput f = (FigureEditorInput) getEditorInput();
			//Canvas canvas = new Canvas(sc, SWT.NONE);
			// canvas.setBackground(FigureSWTApplet.getColor(SWT.COLOR_YELLOW));
			// canvas.setBackgroundMode(SWT.INHERIT_NONE);
			figure = f.getFig();
			if (figure instanceof IConstructor)
							fpa = new FigureSWTApplet(parent,
					(IConstructor) figure, f.getCtx());
			fpa.setSize(parent.getClientArea().width,parent.getClientArea().height);
			//if (figure instanceof IList)
				//fpa = new FigureSWTApplet(canvas, f.getIString().getValue(),
			//			(IList) figure, f.getCtx());
		} else if (getEditorInput() instanceof FileEditorInput) {
			/*
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
			fpa = new FigureSWTApplet(canvas, (IConstructor) figure, null);
			sc.setContent(canvas);
			title = f.getName();
			*/
	/*
		} else
			return;
		//sc.setMinSize(fpa.getFigureWidth(), fpa.getFigureHeight());
		//sc.pack();
		//

		// Make sure that the frame gets the focus when the editor is brought to
		// the top
//		partListener = new IPartListener2() {
//
//			public void partActivated(IWorkbenchPartReference partRef) {
//			}
//
//			public void partBroughtToTop(IWorkbenchPartReference partRef) {
//			}
//
//			public void partClosed(IWorkbenchPartReference partRef) {
//			}
//
//			public void partDeactivated(IWorkbenchPartReference partRef) {
//			}
//
//			public void partHidden(IWorkbenchPartReference partRef) {
//			}
//
//			public void partVisible(IWorkbenchPartReference partRef) {
//			}
//
//			public void partOpened(IWorkbenchPartReference partRef) {
//			}
//
//			public void partInputChanged(IWorkbenchPartReference partRef) {
//			}
//		};
//		getSite().getPage().addPartListener(partListener);
		final MyPrintAction printAction = new MyPrintAction(this);
		getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.PRINT.getId(),
		printAction); 
	}
	*/
}
