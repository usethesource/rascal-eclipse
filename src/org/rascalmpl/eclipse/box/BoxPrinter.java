package org.rascalmpl.eclipse.box;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Stack;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.eclipse.uri.BundleURIResolver;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.library.box.MakeBox;
import org.rascalmpl.uri.ClassResourceInputOutput;
import org.rascalmpl.uri.URIResolverRegistry;

public class BoxPrinter {
	/*
	 * Printing example snippet: print text to printer, with word wrap and
	 * pagination
	 * 
	 * For a list of all SWT example snippets see
	 * http://www.eclipse.org/swt/snippets/
	 */
	static Printer printer;

	private String outputFile, outputDir;
	
	
	final private MakeBox makeBox = new MakeBox();
	

	// Display display;

	Color foregroundColor, backgroundColor;

	FontData[] printerFontData;
	RGB printerForeground, printerBackground;

	int printerLineHeight = 0, displayLineHeight = 0;
	int printerTabWidth = 0, displayTabWidth = 0;
	int printerLeftMargin, printerRightMargin, printerTopMargin,
			printerBottomMargin;
	int displayLeftMargin, displayRightMargin, displayTopMargin,
			displayBottomMargin;
	int x, y;
	int index, end;
	private String textToPrint;
	final private String tabs;
	static private Display screen = Display.getCurrent() == null ? new Display()
			: Display.getCurrent();
	static private Shell shell;
	private Canvas canvas;
	private Image image;
	private ScrollBar hBar, vBar;

	final Point origin = new Point(0, 0);

	// private String fileName;

	private int leftMargin() {
		return printer == null ? displayLeftMargin : printerLeftMargin;
	}

	private int rightMargin() {
		return printer == null ? displayRightMargin : printerRightMargin;
	}

	private int topMargin() {
		return printer == null ? displayTopMargin : printerTopMargin;
	}

	private int bottomMargin() {
		return printer == null ? displayBottomMargin : printerBottomMargin;
	}

	public static void main(String[] args) {
		final BoxPrinter boxPrinter = new BoxPrinter()/* .open() */;
		boxPrinter.open("toLatex");
		close();
	}
	

	static public void close() {
		while (shell != null && !shell.isDisposed()) {
			if (!screen.readAndDispatch())
				screen.sleep();
		}
		if (screen != null)
			screen.dispose();
	}

	private void init(Canvas canvas) {
		Rectangle clientArea = canvas.getShell().getClientArea();
		Rectangle trim = canvas.computeTrim(0, 0, 0, 0);
		Point dpi = canvas.getDisplay().getDPI();
		displayLeftMargin = dpi.x + trim.x; // one inch from left side of paper
		displayRightMargin = clientArea.width - dpi.x + trim.x + trim.width; // one
		// paper
		displayTopMargin = dpi.y + trim.y; // one inch from top edge of paper
		displayBottomMargin = clientArea.height - dpi.y + trim.y + trim.height;
	}

	static URI getFileName() {
		FileDialog dialog = new FileDialog(shell);
//		String[] filterExtensions = new String[] { "*.asf" };
//		dialog.setFilterExtensions(filterExtensions);
	    String defaultDir = System.getProperty("DEFAULTDIR");
		if (defaultDir==null)  defaultDir = System.getProperty("user.home");/*+File.separatorChar+"asfix";*/
		if (defaultDir != null)
			dialog.setFilterPath(defaultDir);
		dialog.setFileName("Concrete.rsc");
		String fileName = dialog.open();
		if (fileName == null) {
			System.err.println("Canceled");
			System.exit(0);
		}
		try {
			URI r = new URI("file", fileName, null);
			System.err.println("uri:"+r);
			return r;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			return null;
		}

	}
	
	public BoxPrinter() {
		this(null);
	}
	

	public BoxPrinter(IProject project) {
		Evaluator ev = makeBox.getCommandEvaluator();
		ProjectURIResolver resolver = new ProjectURIResolver();
		URIResolverRegistry resolverRegistry = ev.getResolverRegistry();
		resolverRegistry.registerInput(resolver);
		resolverRegistry.registerOutput(resolver);
		
		ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(resolverRegistry, "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
		resolverRegistry.registerInput(eclipseResolver);
		ev.addRascalSearchPath(URI.create(eclipseResolver.scheme() + ":///"));
		ev.addClassLoader(getClass().getClassLoader());
		if (project != null) {
			try{
				ev.addRascalSearchPath(new URI("project://" + project.getName() + "/" + IRascalResources.RASCAL_SRC));
			}catch(URISyntaxException usex){
				throw new RuntimeException(usex);
			}
		}
		try {
			String rascalPlugin = FileLocator.resolve(Platform.getBundle("rascal").getEntry("/")).getPath();
			String PDBValuesPlugin = FileLocator.resolve(Platform.getBundle("org.eclipse.imp.pdb.values").getEntry("/")).getPath();
			Configuration.setRascalJavaClassPathProperty(rascalPlugin + File.pathSeparator + PDBValuesPlugin + File.pathSeparator + rascalPlugin + File.separator + "src" + File.pathSeparator + rascalPlugin + File.separator + "bin" + File.pathSeparator + PDBValuesPlugin + File.separator + "bin");
		} catch (IOException e) {
			Activator.getInstance().logException("could not create classpath for parser compilation", e);
		}
		BundleURIResolver bundleResolver = new BundleURIResolver(resolverRegistry);
		resolverRegistry.registerInput(bundleResolver);
		resolverRegistry.registerOutput(bundleResolver);
		int tabSize = 4; // is tab width a user setting in your UI?
		StringBuffer tabBuffer = new StringBuffer(tabSize);
		for (int i = 0; i < tabSize; i++)
			tabBuffer.append(' ');
		tabs = tabBuffer.toString();
		// System.err.println("new BoxPrinter");
	}

	void readRawText(String fileName) {
		File file = new File(fileName);
		FileInputStream stream;
		try {
			stream = new FileInputStream(file.getPath());
			Reader in = new BufferedReader(new InputStreamReader(stream));
			char[] readBuffer = new char[2048];
			StringBuffer buffer = new StringBuffer((int) file.length());
			int n;
			while ((n = in.read(readBuffer)) > 0) {
				buffer.append(readBuffer, 0, n);
			}
			textToPrint = buffer.toString();
			stream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void preparePrint(URI uri) {
		textToPrint = getRichText(uri);
	}
    
	public void preparePrint(URI uri, String ext) {
		textToPrint = getRichText(uri, ext);
	}
	
	private boolean readData(String cmd, URI uri) {
		System.err.println("readData:" + cmd + " "+ uri);
		textToPrint = makeBox.toPrint(cmd, uri);
		return true;
		// System.err.println("MakeBox finished2");
	}
	
	
	
	public String getRichText(URI uri) {
		return makeBox.toRichText(uri);
		/*
		readData("toLatex", uri, true);
		return textToPrint;
		*/
	}
	
	public String getRichText(URI uri, String ext) {
		return makeBox.toRichText(uri, ext);
		/*
		readData("toLatex", uri, true);
		return textToPrint;
		*/
	}

	private void setMenuBar() {
		final Menu menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);
		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("&File");
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		item.setMenu(fileMenu);

		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("N&ew");
		item.setAccelerator(SWT.CTRL + 'N');
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				menuNew();
			}
		});

		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("P&rint");
		item.setAccelerator(SWT.CTRL + 'P');
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				menuPrint();
			}
		});

		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("S&ave");
		item.setAccelerator(SWT.CTRL + 'S');
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				menuSave();
			}
		});

		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("E&xit");
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.close();
				// shell.dispose();
				// shell = null;
				// System.exit(0);
			}
		});
	}

	private void initShell() {
		hBar = canvas.getHorizontalBar();
		vBar = canvas.getVerticalBar();

		shell.setText("Print Text");

		for (Listener q : hBar.getListeners(SWT.Selection)) {
			hBar.removeListener(SWT.Selection, q);
		}
		hBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				int hSelection = hBar.getSelection();
				int destX = -hSelection - origin.x;
				Rectangle rect = image.getBounds();
				canvas.scroll(destX, 0, 0, 0, rect.width, rect.height, false);
				origin.x = -hSelection;
			}
		});
		for (Listener q : vBar.getListeners(SWT.Selection)) {
			hBar.removeListener(SWT.Selection, q);
		}
		vBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				int vSelection = vBar.getSelection();
				int destY = -vSelection - origin.y;
				Rectangle rect = image.getBounds();
				canvas.scroll(0, destY, 0, 0, rect.width, rect.height, false);
				origin.y = -vSelection;
			}
		});
		for (Listener q : canvas.getListeners(SWT.Resize)) {
			canvas.removeListener(SWT.Resize, q);
		}
		canvas.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				adjustHandles(image);
				canvas.redraw();
			}
		});
		canvas.addListener(SWT.Dispose, new Listener() {
			public void handleEvent(Event e) {
				shell.close();
				shell.dispose();
				shell = null;
			}
		});
		for (Listener q : canvas.getListeners(SWT.Show)) {
			canvas.removeListener(SWT.Show, q);
		}
		canvas.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event e) {
				adjustHandles(image);
				canvas.redraw();
			}
		});
		for (Listener q : canvas.getListeners(SWT.Paint)) {
			canvas.removeListener(SWT.Paint, q);
		}
		canvas.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e) {
				e.gc.drawImage(image, origin.x, origin.y);
				Rectangle rect = image.getBounds();
				Rectangle client = canvas.getClientArea();
				int marginWidth = client.width - rect.width;
				if (marginWidth > 0) {
					e.gc.fillRectangle(rect.width, 0, marginWidth,
							client.height);
				}
				int marginHeight = client.height - rect.height;
				if (marginHeight > 0) {
					e.gc.fillRectangle(0, rect.height, client.width,
							marginHeight);
				}
			}
		});
		canvas.addListener(SWT.MenuDetect, new Listener() {
			public void handleEvent(Event event) {
				// Label label = new Label(canvas, SWT.NONE);
				// label.setForeground(bgColor);
				Menu menu = new Menu(canvas);
				// Menu menu = new Menu(shell, SWT.POP_UP);
				{
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText("P&rint");
					item.setAccelerator(SWT.CTRL + 'P');
					item.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							menuPrint();
						}
					});
				}
				{
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText("S&ave");
					item.setAccelerator(SWT.CTRL + 'S');
					item.addSelectionListener(new SelectionAdapter() {
						public void widgetSelected(SelectionEvent event) {
							menuSave();
						}
					});
				}
				menu.setLocation(event.x, event.y);
				menu.setVisible(true);
				while (!menu.isDisposed() && menu.isVisible()) {
					if (!screen.readAndDispatch())
						screen.sleep();
				}
				menu.dispose();
			}
		});
	}

	public void open(String cmd) {
		// System.err.println("SHELL:"+shell);
		if (shell == null) {
			shell = new Shell(screen);
			shell.setLayout(new FillLayout());
			this.canvas = new Canvas(shell, SWT.NO_BACKGROUND
					| SWT.NO_REDRAW_RESIZE | SWT.H_SCROLL | SWT.V_SCROLL);
		}
		URI uri;
		while (!readData(cmd, uri = getFileName()))
			;
		shell.setText(new File(uri.getPath()).getName());
		_open(uri);
		setMenuBar();
		shell.open();
		canvas.redraw();
	}

//	public void open(URI uri, Canvas canvas) {
//		// IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
//		// System.err.println("OK this is this:"+workspace.getProject("box").getFolder("src"));
//		// IFolder folder = workspace.getProject("box").getFolder("src");
//		// System.err.println("HOI:"+folder.exists()+" "+folder.getLocationURI());
//
//		// FileDialog dialog = new FileDialog(shell);
//		// dialog.setFilterPath(string);
//		readData(uri, false);
//		shell = canvas.getShell();
//		screen = shell.getDisplay();
//		this.canvas = canvas;
//		_open(uri);
//		this.canvas.redraw();
//	}

	private void _open(URI uri) {
        File f = new File(uri.getPath());
		outputDir = f.getParent();
		outputFile = f.getName().substring(0, f.getName().lastIndexOf('.'))+".pp.rsc";
		this.canvas.setBackground(screen.getSystemColor(SWT.COLOR_WHITE));
		init(this.canvas);
		Rectangle r = printText(null);
		// System.err.println("Make image:" + r.width + " " + r.height + " "
		// + topMargin());
		image = new Image(screen, r.width, r.height + topMargin());
		if (hBar == null)
			initShell();
		final GC gc = new GC(image);
		setTag(gc, Box.TAG.DF);
		displayTabWidth = gc.stringExtent(tabs).x;
		displayLineHeight = gc.getFontMetrics().getHeight();
		printText(gc);
		adjustHandles(image);

		// outputFile = new File(System.getProperty("user.home") +
		// File.separator
		// + "box", new File(uri.getPath()).getName());
		// System.err.println("Hallo:" + outputFile);
		canvas.redraw();
	}

	private void setTag(GC gc, Box.TAG tag) {
		gc.setFont(printer != null ? tag.printerFont : tag.displayFont);
		gc.setForeground(tag.color);
	}

	private void adjustHandles(Image image) {
		Rectangle rect = image.getBounds();
		Rectangle client = canvas.getClientArea();
		hBar.setMaximum(rect.width);
		vBar.setMaximum(rect.height);
		hBar.setThumb(Math.min(rect.width, client.width));
		vBar.setThumb(Math.min(rect.height, client.height));
		int hPage = rect.width - client.width;
		int vPage = rect.height - client.height;
		int hSelection = hBar.getSelection();
		int vSelection = vBar.getSelection();
		// System.err.println("adjust:"+hSelection+" "+hPage+" "+vSelection+" "+vPage);
		if (hSelection >= hPage) {
			if (hPage <= 0)
				hSelection = 0;
			origin.x = -hSelection;
		}
		if (vSelection >= vPage) {
			if (vPage <= 0)
				vSelection = 0;
			origin.y = -vSelection;
		}
	}

	public void menuPrint() {
		if (shell==null) shell = new Shell(screen);
		PrintDialog dialog = new PrintDialog(shell, SWT.PRIMARY_MODAL);
		PrinterData data = dialog.open();
		if (data == null)
			return;
		printer = new Printer(data);
		Thread printingThread = new Thread("Printing") {
			public void run() {
				print();
				printer.dispose();
				printer = null;
			}
		};
		printingThread.start();
	}

	public void menuSave() {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setFilterPath(outputDir);
        dialog.setFileName(outputFile);
		String fileName = dialog.open();
		save(new File(fileName));
	}

	public void menuNew() {
		shell.setVisible(false);
		open("toLatex");
	}

	private void print() {
		if (printer.startJob("Text")) { // the string is the job name - shows up
			// in the printer's job list
			Rectangle clientArea = printer.getClientArea();
			Rectangle trim = printer.computeTrim(0, 0, 0, 0);
			Point dpi = printer.getDPI();
			printerLeftMargin = dpi.x + trim.x; // one inch from left side of
			// paper
			printerRightMargin = clientArea.width - dpi.x + trim.x + trim.width; // one
			// paper
			printerTopMargin = dpi.y + trim.y; // one inch from top edge of
			// paper
			printerBottomMargin = clientArea.height - dpi.y + trim.y
					+ trim.height; // one

			/*
			 * Create printer GC, and create and set the printer font &
			 * foreground color.
			 */
			final GC gc = new GC(printer);
			setTag(gc, Box.TAG.DF);
			printerTabWidth = gc.stringExtent(tabs).x;
			printerLineHeight = gc.getFontMetrics().getHeight();
			// System.err.println("LH:"+lineHeight);

			/* Print text to current gc using word wrap */
			printText(gc);
			printer.endJob();
			gc.dispose();
		}
	}

	
	

	void save(File f) {
		String[] data = textToPrint.split("\r.{3}");
		try {
			PrintStream s = new PrintStream(f);
			for (String a : data) {
				s.print(a);
			}
			s.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Rectangle printText(GC gcc) {
		final Stack<Box.TAG> stack = new Stack<Box.TAG>();
		boolean newGC = false;
		GC gc;
		if (gcc == null) {
			gc = new GC(screen);
			setTag(gc, Box.TAG.DF);
			displayTabWidth = gc.stringExtent(tabs).x;
			displayLineHeight = gc.getFontMetrics().getHeight();
			newGC = true;
		} else
			gc = gcc;
		Box.TAG current = Box.TAG.DF;
		setTag(gc, current);
		if (textToPrint == null)
			return null;
		if (printer != null)
			printer.startPage();
		x = leftMargin();
		y = topMargin();
		index = 0;
		end = textToPrint.length();
		StringTokenizer t = new StringTokenizer(textToPrint, "\n\r", true);
		while (t.hasMoreTokens()) {
			String c = t.nextToken();
			if (c.equals("\n")) {
				newline();
			} else if (c.equals("\r")) {
				c = t.nextToken();
				if (c.charAt(0) == '{') {
					stack.push(current);
					String key = c.substring(1, 3);
					current = Box.TAG.valueOf(key);
					setTag(gc, current);
				} else if (c.charAt(0) == '}') {
					current = stack.pop();
					setTag(gc, current);
				}
				printWord(gc, c.substring(3));
			} else {
				printWord(gc, c);
			}
		}
		if (printer != null && y + printerLineHeight <= bottomMargin()) {
			printer.endPage();
		}
		if (newGC)
			gc.dispose();
		return new Rectangle(0, 0, rightMargin(), y);
	}

	void printWord(GC gc, String c) {
		if (c.length() > 0) {
			int wordWidth = gc.stringExtent(c).x;
			if (x + wordWidth > rightMargin()) {
				/* word doesn't fit on current line, so wrap */
				newline();
			}
			gc.drawString(c, x, y, false);
			index += c.length();
			x += wordWidth;
		}
	}

	void newline() {
		x = leftMargin();
		y += printer != null ? printerLineHeight : displayLineHeight;
		if (printer != null) {
			if (y + printerLineHeight > bottomMargin()) {
				printer.endPage();
				if (index + 1 < end) {
					y = topMargin();
					printer.startPage();
				}
			}
		}
	}
	
	void updateFont(Font f) {
		Box.updateFont(f);
	}

}
