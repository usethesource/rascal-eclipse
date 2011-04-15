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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.printing.Printer;
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
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.box.BoxPrinter;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.library.vis.FigurePApplet;
import org.rascalmpl.library.vis.IFigureApplet;


public class FigureViewer extends EditorPart {
	protected static final String editorId  = "rascal-eclipse.Figure.viewer";
	
	private IFigureApplet fpa ;
	private ScrolledComposite sc;
	
	private IPartListener2 partListener;

	private static Image makeSWTImage(Display display, java.awt.Image ai)
			throws Exception {
		int width = ai.getWidth(null);
		int height = ai.getHeight(null);
		BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		g2d.drawImage(ai, 0, 0, null);
		g2d.dispose();
		int[] data = ((DataBufferInt) bufferedImage.getData().getDataBuffer())
				.getData();
		ImageData imageData = new ImageData(width, height, 24, new PaletteData(
				0xFF0000, 0x00FF00, 0x0000FF));
		imageData.setPixels(0, 0, data.length, data, 0);
		org.eclipse.swt.graphics.Image swtImage = new Image(display, imageData);
		return swtImage;
	}

	public FigureViewer() {
		super();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {}

	@Override
	public void doSaveAs() {}

	public void print(Printer printer) {
		if (printer.startJob("FigureViewer")) {
		java.awt.Image image = ((FigurePApplet) fpa).getImage();
		GC gc = new GC(printer);
		try {
			org.eclipse.swt.graphics.Image im = makeSWTImage(getSite()
					.getShell().getDisplay(), image);
			gc.drawImage(im, 20, 20);
			// gc.drawString("HALLO", 10,  10);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		printer.endJob();
		gc.dispose();
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		// System.err.println("QQQ:"+site.getId());
		if (input instanceof FigureEditorInput) {
			setSite(site);
			setInput(input);
		} 
		else 
		if (input instanceof FileEditorInput) {
				setInput(input);
			} 
		else {
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

	@SuppressWarnings("serial")
	@Override
	public void createPartControl(Composite parent) {
		
		//final int defaultWidth = 400;
		//final int defaultHeight = 400;
		
		final String title;
		if (getEditorInput() instanceof FigureEditorInput) {
			FigureEditorInput f = (FigureEditorInput) getEditorInput();
		    fpa = new FigurePApplet(f.getIString(), f.getFig(), f.getCtx());
		    title=fpa.getName();
		}
		else if (getEditorInput() instanceof FileEditorInput) {
			FileEditorInput fi = (FileEditorInput) getEditorInput();
			IFile f = fi.getFile();
			String layout = this.getEditorSite().getId();
			int start = layout.lastIndexOf(".");
			layout = layout.substring(start+1);
			URI uri = f.getLocationURI();
		    IProject p = f.getProject();
		    BoxPrinter boxPrinter = new BoxPrinter(p);
		    IConstructor ca = boxPrinter.getFigure(uri, layout);
		    fpa = new FigurePApplet(ca, null);
		    title = f.getName();
		}
		else return;
		
		// Create a scrollable Composite that will contain a new FigurePApplet
		
		sc = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setLayout(new FillLayout());
		sc.setAlwaysShowScrollBars(true);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		this.setPartName(title);
	
		// Set the contents and size of the scrollable Composite and
		// its minimal size
		final AwtChild awtChild = new AwtChild(sc, (FigurePApplet) fpa);
		sc.setContent(awtChild);
		
		// Make sure that the frame gets the focus when the editor is brought to the top
		
		partListener = new IPartListener2(){
			public void partActivated(IWorkbenchPartReference partRef) {
				//System.err.println("partActivated");				
			}

			public void partBroughtToTop(IWorkbenchPartReference partRef) {
				//System.err.println("partBroughtToTop");
				awtChild.getFrame().requestFocusInWindow();
			}

			public void partClosed(IWorkbenchPartReference partRef) {}

			public void partDeactivated(IWorkbenchPartReference partRef) {}

			public void partHidden(IWorkbenchPartReference partRef) {}

			public void partInputChanged(IWorkbenchPartReference partRef) {}

			public void partOpened(IWorkbenchPartReference partRef) {}

			public void partVisible(IWorkbenchPartReference partRef) {}
		};
		
		getSite().getPage().addPartListener(partListener);

		
		int figWidth = fpa.getFigureWidth();
		int figHeight =  fpa.getFigureHeight();
		
		sc.setBounds(0, 0,figWidth, figHeight);
		sc.setMinSize(figWidth, figHeight);
		//sc.setMinSize(defaultWidth, defaultHeight);
		sc.pack();
	}
	
	public void dispose(){
		IWorkbenchPage page = getSite().getPage();
		page.removePartListener(partListener);
		
		fpa = null; // Make the memory leak less severe.
		sc = null; // Make the memory leak less severe.
		
		setInput(null); // Make the memory leak less severe.
		
		super.dispose();
	}

	@Override
	public void setFocus() {}
	
	public static void open(final IString name, final IConstructor fig,  final IEvaluatorContext ctx) {
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
							page.openEditor(new FigureEditorInput(name, fig, ctx),
									editorId);
						} catch (PartInitException e) {
							Activator.getInstance().logException(
									"failed to open Figure viewer", e);
						}

					}
				});
			}
		}
	}
}
