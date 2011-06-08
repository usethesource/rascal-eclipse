/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Bert Lisser - Bert.Lisser@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.box;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.rascalmpl.eclipse.library.vis.MyPrintAction;



public class BoxViewer extends EditorPart {

	public static final String EDITOR_ID = "org.rascalmpl.eclipse.box.boxviewer";
	static final IThemeManager themeManager = PlatformUI.getWorkbench()
	.getThemeManager();
	static final ITheme currentTheme = themeManager.getCurrentTheme();
	static final FontRegistry fontRegistry = currentTheme.getFontRegistry();

	static final private Font displayFont = new Font(Display.getCurrent(),
			new FontData("monospace", 8, SWT.NORMAL));

	
	private TextViewer tv;
	
	private StyledText st;
	
	private BoxProvider boxProvider;
	
	private IDocument document;

	public BoxViewer() {
		super();
	}

	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);	
		boxProvider = new BoxProvider();
		document =  boxProvider.createDocument(getEditorInput());
		FileEditorInput fi = (FileEditorInput) getEditorInput();
		this.setPartName(fi.getName());
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	public void print(Printer printer) {
		final Font printerFont = fontRegistry
		.get("rascal-eclipse.printerFontDefinition");
		final StyledText nst = getPrintStyledText(st, printerFont);
		nst.print(printer).run();
	}
	
	private StyledText getPrintStyledText(final StyledText st, final Font printerFont) {
		StyledTextContent content = st.getContent();
		final StyledText nst = new StyledText(st.getParent(), SWT.READ_ONLY);
		nst.setContent(content);
		nst.setStyleRanges(st.getStyleRanges());
		nst.setFont(printerFont);
		nst.setLineSpacing(2);
		return nst;
	}
		
	@Override
	public void createPartControl(Composite parent) {
		int styles= SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER;
		tv = new TextViewer(parent, styles);
		tv.setEditable(false);
		st = tv.getTextWidget();
		st.setFont(displayFont);
		st.setLineSpacing(2);
		tv.setDocument(document);
		tv.changeTextPresentation(
				new BoxTextRepresentation(document), true);
		final MyPrintAction printAction = new MyPrintAction(this);
        getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.PRINT.getId(),	printAction); 
	}

	@Override
	public void setFocus() {
		return;
		// super.setFocus();
	}


	@Override
	public boolean isSaveOnCloseNeeded() {
		return false;
	}
	
	public StyledText getTextWidget() {
		    return st;
		    }

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}
	
	/*
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
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/

	
	
}
