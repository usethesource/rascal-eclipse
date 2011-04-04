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
			new FontData("monospace", 8, SWT.NORMAL));

	private Shell shell;

	public BoxViewer() {
		super();
	}

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

	

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setDocumentProvider(new BoxProvider());
		setSite(site);
		setInput(input);
		shell = site.getShell();
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
		// shell = st.getShell();
		st.setFont(displayFont);
		st.setLineSpacing(2);
		this.getSourceViewer().changeTextPresentation(
				new BoxTextRepresentation(this.getDocumentProvider()
						.getDocument(getEditorInput())), true);
	}

	@Override
	public void setFocus() {
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
	
	public StyledText getTextWidget() {
		    return this.getSourceViewer().getTextWidget();
		    }
	
	
}
