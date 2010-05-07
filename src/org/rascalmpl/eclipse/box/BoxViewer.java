package org.rascalmpl.eclipse.box;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class BoxViewer extends AbstractTextEditor {

	static final private Font displayFont = new Font(Display.getCurrent(), new FontData("monospace",
			12, SWT.NORMAL));

	public BoxViewer() {
		super();
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
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
		setDocumentProvider(new BoxProvider());
		setSite(site);
		setInput(input);
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
		super.createPartControl(parent);
		StyledText st = this.getSourceViewer().getTextWidget();
		st.setFont(displayFont);
		this.getSourceViewer().changeTextPresentation(
				new BoxTextRepresentation(this.getDocumentProvider().getDocument(getEditorInput())), true);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		return;
		// super.setFocus();
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return false;
	}
	
//	private Canvas canvas;
//	static Color keyColor = getColor(SWT.COLOR_RED);
//	static Color textColor = getColor(SWT.COLOR_BLACK);
//	static Color numColor = getColor(SWT.COLOR_BLUE);
//	public static final String EDITOR_ID = "org.rascalmpl.eclipse.box.boxviewer";
//	public static final String EDITOR_CONTEXT = EDITOR_ID + ".context";
//	public static final String EDITOR_RULER = EDITOR_CONTEXT + ".ruler";
//
//	private static Color getColor(final int which) {
//		Display display = Display.getCurrent();
//		if (display != null)
//			return display.getSystemColor(which);
//		display = Display.getDefault();
//		final Color result[] = new Color[1];
//		display.syncExec(new Runnable() {
//			public void run() {
//				synchronized (result) {
//					result[0] = Display.getCurrent().getSystemColor(which);
//				}
//			}
//		});
//		synchronized (result) {
//			return result[0];
//		}
//	}
	// @Override
	// protected void initializeEditor() {
	// System.err.println("Initialize texteditor");
	// super.initializeEditor();
	// setEditorContextMenuId(EDITOR_CONTEXT);
	// setRulerContextMenuId(EDITOR_RULER);
	//		
	// }
	/*
	 * @Override public void createPartControl(Composite parent) { canvas = new
	 * Canvas(parent, SWT.NO_BACKGROUND // | SWT.NO_REDRAW_RESIZE | SWT.H_SCROLL
	 * | SWT.V_SCROLL); // canvas = (Canvas) this.getSourceViewer();
	 * canvas.setLayout(new FillLayout()); canvas.setVisible(true); IEditorInput
	 * input = getEditorInput(); FileEditorInput f = (FileEditorInput) input; //
	 * System.err.println("Folder:"+ f.getFile().getParent().getLocationURI());
	 * setPartName(f.getFile().getName());
	 * 
	 * // boxPrinter = new BoxPrinter(); // URI uri =
	 * f.getFile().getLocationURI(); // boxPrinter.open(uri, canvas); }
	 */
//	private BoxPrinter boxPrinter;
	//
//		public BoxPrinter getBoxPrinter() {
//			return boxPrinter;
//		}

}
