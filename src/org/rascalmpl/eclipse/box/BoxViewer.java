package org.rascalmpl.eclipse.box;

import java.net.URI;
import java.util.Stack;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
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
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

public class BoxViewer extends EditorPart {

//	@Override
//	public boolean isEditable() {
//		return false;
//	}

	public BoxViewer() {
		super();
		System.err.println("BoxViewer");
		
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
	

	// static private BoxViewer findConsole(String name) {
	// ConsolePlugin plugin = ConsolePlugin.getDefault();
	// IConsoleManager conMan = plugin.getConsoleManager();
	// IConsole[] existing = conMan.getConsoles();
	// for (int i = 0; i < existing.length; i++)
	// if (name.equals(existing[i].getName()))
	// return (BoxViewer) existing[i];
	// //no console found, so create a new one
	// // Display.getCurrent().asyncExec(runnable);
	// BoxViewer myConsole = new BoxViewer(name,
	// ImageDescriptor.getMissingImageDescriptor());
	// conMan.addConsoles(new IConsole[]{myConsole});
	// return myConsole;
	// }
	//	
	// static void run(URI uri) {
	// BoxViewer p = findConsole(CONSOLE_NAME);
	// p.setImageDescriptor(new BoxPrinter().open(uri));
	// p.activate();
	// }

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
//		final IActionBars allActionBars = getEditorSite().getActionBars();
//		IContributionItem[] items = allActionBars.getMenuManager().getItems();
//		for (IContributionItem item : items) {
//			System.err.println(item.getId() + " " + item.getClass());
//			if (item instanceof MenuManager) {
//				MenuManager m = (MenuManager) item;
//				Menu q = m.getMenu();
//				System.err.println("Hallo1:" + q);
//				if (q != null) {
//					System.err.println("Hallo2:" + m.getSize());
//					System.err.println("Hallo3:" + q.getItemCount());
//					IContributionItem[] a = m.getItems();
//					for (IContributionItem b : a) {
//						if (b instanceof CommandContributionItem) {
//							CommandContributionItem c = (CommandContributionItem) b;
//							System.err.println(b.getId() + " " + b.getClass());
//							System.err.println(c.getCommand());
//							if (c.getCommand() != null) {
//								System.err.println(c.getCommand().getCommand());
//							}
//						}
//					}
//				}
//			}
//		}
	}

	//	
	// String id = ActionFactory.PRINT.getId();
	// IContributionItem[] items = allActionBars.getMenuManager().getItems();
	// IAction printAction = new Action() {};
	// site.getActionBars().setGlobalActionHandler(.getId(), printAction);
	// IActionBars actionBars = site.getActionBars();
	// actionBars.getMenuManager().add();
	// actionBars.updateActionBars();
	// PlatformUI.getWorkbench().

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

	@Override
	public void createPartControl(Composite parent) {
		canvas = new Canvas(parent, SWT.NO_BACKGROUND /* | SWT.NO_REDRAW_RESIZE */
				| SWT.H_SCROLL | SWT.V_SCROLL);
		canvas.setLayout(new FillLayout());
		canvas.setVisible(true);
		IEditorInput input = getEditorInput();
		FileEditorInput f = (FileEditorInput) input;
		URI uri = f.getFile().getLocationURI();
		setPartName(f.getFile().getName());
		boxPrinter = new BoxPrinter();
		boxPrinter.open(uri, canvas);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		canvas.setFocus();
	}

	// public IPageBookViewPage createPage(IConsoleView view) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	
	
	
	@Override
	public  boolean isSaveOnCloseNeeded() {return false;}
}
