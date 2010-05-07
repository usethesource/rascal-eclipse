package org.rascalmpl.eclipse.box;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.StorageDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.rascalmpl.library.box.BoxPrinter;

public class BoxProvider extends StorageDocumentProvider {

	IDocument d = new BoxDocument();

	@Override
	public IDocument createDocument(Object element) {
		try {
			System.err.println("createDocument:" + getDefaultEncoding());

			setDocumentContent(d, (IEditorInput) element, getDefaultEncoding());
			return d;
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public IDocument getDocument(Object element) {
		return d;
	}

	@Override
	public boolean setDocumentContent(IDocument document,
			IEditorInput editorInput, String encoding) throws CoreException {
		FileEditorInput f = (FileEditorInput) editorInput;
		URI uri = f.getFile().getLocationURI();
		((BoxDocument) document).computeDocument(new BoxPrinter()
				.getRichText(uri));
		return true;
	}

	
}
