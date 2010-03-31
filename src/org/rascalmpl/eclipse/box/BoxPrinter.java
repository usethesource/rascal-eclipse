package org.rascalmpl.eclipse.box;

import java.io.*;
import java.util.Stack;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.printing.*;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import java.net.URI;
import java.net.URISyntaxException;

public class BoxPrinter {
	/*
	 * Printing example snippet: print text to printer, with word wrap and
	 * pagination
	 * 
	 * For a list of all SWT example snippets see
	 * http://www.eclipse.org/swt/snippets/
	 */
	enum TAG {
		it, nm, bf, start;
	}

	Display display;

	Color foregroundColor, backgroundColor;

	Printer printer;
	GC gc;
	FontData[] printerFontData;
	RGB printerForeground, printerBackground;

	int lineHeight = 0;
	int tabWidth = 0;
	int leftMargin, rightMargin, topMargin, bottomMargin;
	int x, y;
	int index, end;
	String textToPrint;
	String tabs;
	StringBuffer wordBuffer;
	final Display screen;
	final Shell shell;
	final Canvas canvas;
	Image image;
	final Point origin = new Point(0, 0);
	private final String fileName;
	final ScrollBar vBar, hBar;

	public static void main(String[] args) {
		final BoxPrinter boxPrinter = new BoxPrinter()/* .open() */;
		boxPrinter.open();
		boxPrinter.close();
	}

	private void close() {
		while (!shell.isDisposed()) {
			if (!screen.readAndDispatch())
				screen.sleep();
		}
		screen.dispose();
	}

	private void init(Canvas canvas) {
		Rectangle clientArea = canvas.getShell().getClientArea();
		Rectangle trim = canvas.computeTrim(0, 0, 0, 0);
		Point dpi = canvas.getDisplay().getDPI();
		leftMargin = dpi.x + trim.x; // one inch from left side of paper
		rightMargin = clientArea.width - dpi.x + trim.x + trim.width; // one
		// paper
		topMargin = dpi.y + trim.y; // one inch from top edge of paper
		bottomMargin = clientArea.height - dpi.y + trim.y + trim.height;
		int tabSize = 4; // is tab width a user setting in your UI?
		StringBuffer tabBuffer = new StringBuffer(tabSize);
		for (int i = 0; i < tabSize; i++)
			tabBuffer.append(' ');
		tabs = tabBuffer.toString();
		GC gc = new GC(canvas);
		tabWidth = gc.stringExtent(tabs).x;
		lineHeight = gc.getFontMetrics().getHeight();
		gc.dispose();
	}

	BoxPrinter() {
		screen = Display.getCurrent() == null ? new Display() : Display
				.getCurrent();
		shell = new Shell(screen);
		shell.setLayout(new FillLayout());
		canvas = new Canvas(shell, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE
				| SWT.H_SCROLL | SWT.V_SCROLL);

		hBar = canvas.getHorizontalBar();
		vBar = canvas.getVerticalBar();
		canvas.setBackground(screen.getSystemColor(SWT.COLOR_WHITE));
		init(canvas);
		shell.setText("Print Text");
		Menu menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);

		MenuItem item = new MenuItem(menuBar, SWT.CASCADE);
		item.setText("&File");
		Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
		item.setMenu(fileMenu);
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("P&rint");
		item.setAccelerator(SWT.CTRL + 'P');
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				menuPrint();
			}
		});
		item = new MenuItem(fileMenu, SWT.PUSH);
		item.setText("E&xit");
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				System.exit(0);
			}
		});

		shell.open();

		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		String[] filterExtensions = new String[] { "*.rsc" };
		dialog.setFilterExtensions(filterExtensions);
		String defaultDir = System.getProperty("DEFAULTDIR");
		if (defaultDir != null)
			dialog.setFilterPath(defaultDir);
		fileName = dialog.open();
		if (fileName == null) {
			System.err.println("Canceled");
			System.exit(0);
		}
		try {
			URI uri = new URI("file", fileName, null);
			IValue v = new MakeBox().run(uri, null);
			textToPrint = toString(v);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	void open() {
		if (fileName == null)
			return;
		// readRawText(fileName);
		
		hBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if (image == null)
					return;
				int hSelection = hBar.getSelection();
				int destX = -hSelection - origin.x;
				Rectangle rect = image.getBounds();
				canvas.scroll(destX, 0, 0, 0, rect.width, rect.height, false);
				origin.x = -hSelection;
			}
		});
		vBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if (image == null)
					return;
				int vSelection = vBar.getSelection();
				int destY = -vSelection - origin.y;
				Rectangle rect = image.getBounds();
				canvas.scroll(0, destY, 0, 0, rect.width, rect.height, false);
				origin.y = -vSelection;
			}
		});
		canvas.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				if (image == null)
					return;
				adjustHandles();
				canvas.redraw();
			}
		});
		canvas.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e) {
				if (image == null)
					return;
				GC gc = e.gc;
				gc.drawImage(image, origin.x, origin.y);
				Rectangle rect = image.getBounds();
				Rectangle client = canvas.getClientArea();
				int marginWidth = client.width - rect.width;
				if (marginWidth > 0) {
					gc.fillRectangle(rect.width, 0, marginWidth, client.height);
				}
				int marginHeight = client.height - rect.height;
				if (marginHeight > 0) {
					gc
							.fillRectangle(0, rect.height, client.width,
									marginHeight);
				}
			}
		});
		Rectangle r = printText(null);
		image = new Image(screen, r.width, r.height + topMargin);
		adjustHandles();
		gc = new GC(image);
		// setStyle(gc, SWT.ITALIC);
		setTag(gc, TAG.it);
		printText(gc);
		canvas.redraw();
	}

	private void setStyle(GC gc, int style) {
		FontData[] data = gc.getFont().getFontData();
		for (int i = 0; i < data.length; i++) {
			data[i].setStyle(style);
		}
		Font newFont = new Font(screen, data);
		gc.setFont(newFont);
	}

	private void setTag(GC gc, TAG tag) {
		switch (tag) {
		case bf:
			setStyle(gc, SWT.BOLD);
			gc.setForeground(textColor);
			return;
		case it:
			setStyle(gc, SWT.ITALIC);
			gc.setForeground(textColor);
			return;
		case nm:
			setStyle(gc, SWT.NORMAL);
			gc.setForeground(numColor);
			return;
		case start:
			setStyle(gc, SWT.NORMAL);
			gc.setForeground(textColor);
			return;
		}
	}

	private void adjustHandles() {
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

	void menuPrint() {
		PrintDialog dialog = new PrintDialog(shell, SWT.NONE);
		PrinterData data = dialog.open();
		if (data == null)
			return;
		if (data.printToFile) {
			data.fileName = "print.out"; // you probably want to ask the user
			// for a filename
		}

		printer = new Printer(data);
		Thread printingThread = new Thread("Printing") {
			public void run() {
				print(printer);
				printer.dispose();
			}
		};
		printingThread.start();
	}

	void print(Printer printer) {
		if (printer.startJob("Text")) { // the string is the job name - shows up
			// in the printer's job list
			Rectangle clientArea = printer.getClientArea();
			Rectangle trim = printer.computeTrim(0, 0, 0, 0);
			Point dpi = printer.getDPI();
			leftMargin = dpi.x + trim.x; // one inch from left side of paper
			rightMargin = clientArea.width - dpi.x + trim.x + trim.width; // one
			// paper
			topMargin = dpi.y + trim.y; // one inch from top edge of paper
			bottomMargin = clientArea.height - dpi.y + trim.y + trim.height; // one
			int tabSize = 4; // is tab width a user setting in your UI?
			StringBuffer tabBuffer = new StringBuffer(tabSize);
			for (int i = 0; i < tabSize; i++)
				tabBuffer.append(' ');
			tabs = tabBuffer.toString();

			/*
			 * Create printer GC, and create and set the printer font &
			 * foreground color.
			 */
			gc = new GC(printer);
			Font printerFont = new Font(printer, printerFontData);
			Color printerForegroundColor = new Color(printer, printerForeground);
			Color printerBackgroundColor = new Color(printer, printerBackground);

			gc.setFont(printerFont);
			gc.setForeground(printerForegroundColor);
			gc.setBackground(printerBackgroundColor);
			tabWidth = gc.stringExtent(tabs).x;
			lineHeight = gc.getFontMetrics().getHeight();

			/* Print text to current gc using word wrap */
			printText(gc);
			printer.endJob();

			/* Cleanup graphics resources used in printing */
			printerFont.dispose();
			printerForegroundColor.dispose();
			printerBackgroundColor.dispose();
			gc.dispose();
		}
	}

	private String toString(IValue v) {
		IList rules = (IList) v;
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < rules.length(); i++) {
			b.append(((IString) rules.get(i)).getValue());
			b.append("\n");
		}
		return b.toString();
	}

	private Rectangle printText(GC gcc) {
		final Stack<TAG> stack = new Stack<TAG>();
		boolean newGC = false;
		if (gcc == null) {
			gc = new GC(screen);
			newGC = true;
		} else
			gc = gcc;
		TAG current = TAG.start;
		setTag(gc, current);
		if (textToPrint == null)
			return null;
		if (printer != null)
			printer.startPage();
		wordBuffer = new StringBuffer();
		x = leftMargin;
		y = topMargin;
		index = 0;
		end = textToPrint.length();
		StringTokenizer t = new StringTokenizer(textToPrint, "\n\b", true);
		while (t.hasMoreTokens()) {
			String c = t.nextToken();
			if (c.equals("\n")) {
				newline();
			} else if (c.equals("\b")) {
				c = t.nextToken();
				if (c.charAt(0) == '{') {
					String key = c.substring(1, 3);
					if (key.equals(TAG.bf.name())) {
						stack.push(current);
						setTag(gc, TAG.bf);
						current = TAG.bf;
					}
					if (key.equals(TAG.it.name())) {
						stack.push(current);
						setTag(gc, TAG.it);
						current = TAG.it;
					}
					if (key.equals(TAG.nm.name())) {
						stack.push(current);
						setTag(gc, TAG.nm);
						current = TAG.nm;
					}
				} else if (c.charAt(0) == '}') {
					current = stack.pop();
					setTag(gc, current);
				}
				printWord(gc, c.substring(3));
			} else {
				printWord(gc, c);
			}
		}
		if (printer != null && y + lineHeight <= bottomMargin) {
			printer.endPage();
		}
		if (newGC)
			gc.dispose();
		return new Rectangle(0, 0, rightMargin, y);
	}

	void printWord(GC gc, String c) {
		if (c.length() > 0) {
			int wordWidth = gc.stringExtent(c).x;
			if (x + wordWidth > rightMargin) {
				/* word doesn't fit on current line, so wrap */
				newline();
			}
			gc.drawString(c, x, y, false);
			index += c.length();
			x += wordWidth;
		}
	}

	void newline() {
		x = leftMargin;
		y += lineHeight;
		if (printer != null) {
			if (y + lineHeight > bottomMargin) {
				printer.endPage();
				if (index + 1 < end) {
					y = topMargin;
					printer.startPage();
				}
			}
		}

	}

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

	static Color keyColor = getColor(SWT.COLOR_RED);
	static Color textColor = getColor(SWT.COLOR_BLACK);
	static Color numColor = getColor(SWT.COLOR_BLUE);
}
