package org.rascalmpl.eclipse.box;

import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.rascalmpl.eclipse.box. BoxPrinter;

public class BoxProvider extends FileDocumentProvider {

	IDocument d = new BoxDocument();

	@Override
	public IDocument createDocument(Object element) {
		try {
			// System.err.println("createDocument:" + getDefaultEncoding());

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
