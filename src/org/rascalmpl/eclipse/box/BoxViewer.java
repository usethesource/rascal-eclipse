package org.rascalmpl.eclipse.box;

import java.net.URI;
import java.util.Stack;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.rascalmpl.library.box.BoxPrinter;

public class BoxViewer extends TextEditor {
	

//	@Override
//	public boolean isEditable() {
//		return false;
//	}

	public BoxViewer() {
		super();
		setDocumentProvider(new FileDocumentProvider());
		
		// System.err.println("BoxViewer");
		
	}
	
//	@Override
//	protected void initializeEditor() {
//		System.err.println("Initialize texteditor");
//		super.initializeEditor();
//		setEditorContextMenuId(EDITOR_CONTEXT);
//		setRulerContextMenuId(EDITOR_RULER);
//		
//	}

	private Canvas canvas;
	static Color keyColor = getColor(SWT.COLOR_RED);
	static Color textColor = getColor(SWT.COLOR_BLACK);
	static Color numColor = getColor(SWT.COLOR_BLUE);
	public static final String EDITOR_ID = "org.rascalmpl.eclipse.box.boxviewer";
	public static final String EDITOR_CONTEXT = EDITOR_ID+".context";
	public static final String EDITOR_RULER = EDITOR_CONTEXT+".ruler";


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
	

	

	static void print(MessageConsole myConsole, IValue v) {
		final Stack<MessageConsoleStream> stack = new Stack<MessageConsoleStream>();
		MessageConsoleStream out = myConsole.newMessageStream();
		out.setColor(textColor);
		IList rules = (IList) v;
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < rules.length(); i++) {
			b.append(((IString) rules.get(i)).getValue());
			b.append("\n");
		}
		StringTokenizer t = new StringTokenizer(b.toString(), "\n\b", true);
		while (t.hasMoreTokens()) {
			String c = t.nextToken();
			if (c.equals("\n")) {
				out.println();
			} else if (c.equals("\b")) {
				c = t.nextToken();
				if (c.charAt(0) == '{') {
					String key = c.substring(1, 3);
					if (key.equals("bf")) {
						stack.push(out);
						out = myConsole.newMessageStream();
						out.setFontStyle(SWT.BOLD);
						out.print(c.substring(3));
					}
					if (key.equals("it")) {
						stack.push(out);
						out = myConsole.newMessageStream();
						out.setFontStyle(SWT.ITALIC);
						out.print(c.substring(3));
					}
					if (key.equals("nm")) {
						stack.push(out);
						out = myConsole.newMessageStream();
						out.setColor(numColor);
						out.print(c.substring(3));
					}
				} else if (c.charAt(0) == '}') {
					out = stack.pop();
					if (c.length() > 3)
						out.print(c.substring(3));
				}
			} else {
				out.print(c);
			}
		}

	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		System.err.println("HELP SAVE");
        monitor.setCanceled(true);
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}


	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	// @Override
	// public boolean isPrintAllowed() {
	// return true;
	// }
	
	private BoxPrinter boxPrinter;

	public BoxPrinter getBoxPrinter() {
		return boxPrinter;
	}

/*	@Override
	public void createPartControl(Composite parent) {
	canvas = new Canvas(parent, SWT.NO_BACKGROUND // | SWT.NO_REDRAW_RESIZE 
				| SWT.H_SCROLL | SWT.V_SCROLL);
//		canvas = (Canvas) this.getSourceViewer();
		canvas.setLayout(new FillLayout());
		canvas.setVisible(true);
		IEditorInput input = getEditorInput();
		FileEditorInput f = (FileEditorInput) input;
// System.err.println("Folder:"+ f.getFile().getParent().getLocationURI());
		setPartName(f.getFile().getName());
		
//		boxPrinter = new BoxPrinter();
//		URI uri = f.getFile().getLocationURI();
//		boxPrinter.open(uri, canvas);
	}
*/

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		super.setFocus();
	}

	// public IPageBookViewPage createPage(IConsoleView view) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	
	@Override
	public boolean isEditable() {return false;}
	
	@Override
	public  boolean isSaveOnCloseNeeded() {return false;}
}
