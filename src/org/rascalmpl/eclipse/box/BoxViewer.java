package org.rascalmpl.eclipse.box;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class BoxViewer extends AbstractTextEditor {
	
	public static final String EDITOR_ID = "org.rascalmpl.eclipse.box.boxviewer";

	static final private Font displayFont = new Font(Display.getCurrent(),
			new FontData("Monaco", 12, SWT.NORMAL));

	private Shell shell;

	public BoxViewer() {
		super();
	}

	// @Override
	// public void doSave(IProgressMonitor monitor) {
	// System.err.println("HELP SAVE");
	// monitor.setCanceled(true);
	// }

	// @Override
	// public void doSaveAs() {
	// super.doSaveAs();
	//		
	// // TODO Auto-generated method stub
	// }

	@Override
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		FileEditorInput fi = (FileEditorInput) getEditorInput();
		IProject p = fi.getFile().getProject();
		IFolder dir = p.getFolder("PP");
		if (!dir.exists())
			try {
				dir.create(true, true, progressMonitor);
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		IPath path = workspace.getLocation().append(dir.getFullPath());
		dialog.setFilterPath(path.toPortableString());
		dialog.setFileName(fi.getName());
		String fileName = dialog.open();
		if (fileName == null)
			return;
		try {
			URI uri = new URI("file", fileName, null);
			IFile[] files = workspace.findFilesForLocationURI(uri);
			IFile f = files[0];
			BoxDocument d = (BoxDocument) getDocumentProvider().getDocument(
					getEditorInput());
			ByteArrayInputStream inp = new ByteArrayInputStream(d.get()
					.getBytes());
			if (f.exists())
				f.setContents(inp, true, false, progressMonitor);
			else
				f.create(inp, true, progressMonitor);
			// System.err.println("HELP SAVE AS" + f);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		return true;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		StyledText st = this.getSourceViewer().getTextWidget();
		shell = st.getShell();
		st.setFont(displayFont);
		this.getSourceViewer().changeTextPresentation(
				new BoxTextRepresentation(this.getDocumentProvider()
						.getDocument(getEditorInput())), true);
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

	// private Canvas canvas;
	// static Color keyColor = getColor(SWT.COLOR_RED);
	// static Color textColor = getColor(SWT.COLOR_BLACK);
	// static Color numColor = getColor(SWT.COLOR_BLUE);
	
	// public static final String EDITOR_CONTEXT = EDITOR_ID + ".context";
	// public static final String EDITOR_RULER = EDITOR_CONTEXT + ".ruler";
	//
	// private static Color getColor(final int which) {
	// Display display = Display.getCurrent();
	// if (display != null)
	// return display.getSystemColor(which);
	// display = Display.getDefault();
	// final Color result[] = new Color[1];
	// display.syncExec(new Runnable() {
	// public void run() {
	// synchronized (result) {
	// result[0] = Display.getCurrent().getSystemColor(which);
	// }
	// }
	// });
	// synchronized (result) {
	// return result[0];
	// }
	// }
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
	// private BoxPrinter boxPrinter;
	//
	// public BoxPrinter getBoxPrinter() {
	// return boxPrinter;
	// }

}
