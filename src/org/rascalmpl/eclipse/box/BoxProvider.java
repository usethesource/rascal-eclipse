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
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.box;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;

public class BoxProvider extends FileDocumentProvider {

	private IDocument d = new BoxDocument();

	@Override
	public IDocument createDocument(Object element) {
		// System.err.println("createDocument:" + getDefaultEncoding());
		FileEditorInput f = (FileEditorInput) element;
		String ext = f.getFile().getFileExtension();
		if (ext != null)
			if (ext.equals("rsc"))
				prettyPrintRascal(f.getFile());
			else 
				prettyPrint(f.getFile(), ext);
		return d;
	}

	@Override
	public IDocument getDocument(Object element) {
		return d;
	}

	private void prettyPrintRascal(IFile f) {
		URI uri = f.getLocationURI();
		IProject p = f.getProject();
		BoxPrinter boxPrinter = new BoxPrinter(p);
		((BoxDocument) d).computeDocument(boxPrinter.getRichTextRascal(uri));
	}

	private void prettyPrint(IFile f, String ext) {
		URI uri = f.getLocationURI();
		IProject p = f.getProject();
		BoxPrinter boxPrinter = new BoxPrinter(p);
		((BoxDocument) d).computeDocument(boxPrinter.getRichText(uri, ext));
	}

}
