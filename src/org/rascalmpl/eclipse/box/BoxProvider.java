package org.rascalmpl.eclipse.box;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
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
			else if (ext.equals("pico"))
				prettyPrint(f.getFile(), ext);
			else
				System.err.println("Cannot display extension:"
						+ f.getFile().getFileExtension());
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
		((BoxDocument) d).computeDocument(boxPrinter.getRichText(uri));
	}

	private void prettyPrint(IFile f, String ext) {
		URI uri = f.getLocationURI();
		IProject p = f.getProject();
		BoxPrinter boxPrinter = new BoxPrinter(p);
		((BoxDocument) d).computeDocument(boxPrinter.getRichText(uri, ext));
	}

}
