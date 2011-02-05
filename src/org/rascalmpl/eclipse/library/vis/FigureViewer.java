package org.rascalmpl.eclipse.library.vis;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.box.BoxPrinter;
import org.rascalmpl.library.vis.FigurePApplet;

public class FigureViewer extends EditorPart {
	protected static final String editorId  = "rascal-eclipse.Figure.viewer";
	
	FigurePApplet pa ;

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
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	public void print(Printer printer) {
		if (printer.startJob("FigureViewer")) {
		
		java.awt.Image image = pa.getImage();
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
		    pa = ((FigureEditorInput) getEditorInput())
				.getFigurePApplet();
		    title="Figure";
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
		    pa = new FigurePApplet(ca, null);
		    title = f.getName();
		}
		else return;
		
		// Create a scrollable Composite that will contain a new FigurePApplet
		
		final ScrolledComposite sc = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		
		
		sc.setLayout(new FillLayout());
		sc.setAlwaysShowScrollBars(true);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		
		// Create an embedded composite that will contain the real FigurePApplet
		
		final Composite awtChild = new Composite(sc, SWT.BORDER | SWT.DOUBLE_BUFFERED | SWT.EMBEDDED);
		awtChild.setLayout(new FillLayout());
		
		// Create the FigurePApplet
		
		final FigurePApplet fpa = ((FigureEditorInput) getEditorInput()).getFigurePApplet();
		
		// A frame that forms the actual SWT+AWT bridge
		
		final Frame frame = SWT_AWT.new_Frame(awtChild); 
		frame.setLocation(0,0);
		frame.add(fpa);
		fpa.init();			// Initialize the FigurePApplet

		// Make sure to dispose of the FigurePApplet when this Viewer is closed
		
		awtChild.addDisposeListener(new DisposeListener() {
			 public void widgetDisposed(DisposeEvent event) {
				 fpa.destroy();
			 }
		});
		
		// An extra panel (a hack that is needed on older JDKs)
		
		final Panel panel = new Panel(new BorderLayout()) {
		     @Override
			public void update(java.awt.Graphics g) {
				/* Do not erase the background */
				paint(g);
			}
		};
		frame.add(panel);
		this.setPartName(title);
		frame.setVisible(true);
		frame.pack();
		
		// Set the contents and size of the scrollable Composite and
		// its minimal size
		
		sc.setContent(awtChild);
		
		int figWidth = fpa.getFigureWidth();
		int figHeight =  fpa.getFigureHeight();
		
		sc.setBounds(0, 0,figWidth, figHeight);
		sc.setMinSize(figWidth, figHeight);
		//sc.setMinSize(defaultWidth, defaultHeight);
		sc.pack();
	}

	@Override
	public void setFocus() {
	}

	public static void open(final FigurePApplet applet) {
		if (applet == null) {
			return;
		}
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
							page.openEditor(new FigureEditorInput(applet),
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
