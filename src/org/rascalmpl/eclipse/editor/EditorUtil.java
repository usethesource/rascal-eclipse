package org.rascalmpl.eclipse.editor;

import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.uri.URIEditorInput;
import org.rascalmpl.eclipse.uri.URIResourceResolver;
import org.rascalmpl.eclipse.uri.URIStorage;
import org.rascalmpl.interpreter.control_exceptions.Throw;

public class EditorUtil {
	
	public static boolean openAndSelectURI(ISourceLocation loc) {
		if (loc.hasOffsetLength()) {
			return openAndSelectURI(loc.getURI(), loc.getOffset(), loc.getLength());
		}
		return openAndSelectURI(loc.getURI());
	}

	public static boolean openAndSelectURI(URI uri) {
		return openAndSelectURI(uri, -1, 0);
	}

	public static boolean openAndSelectURI(URI uri, int offset, int length) {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			IResource res = URIResourceResolver.getResource(uri);
			if (res != null && res instanceof IFile) {
				IEditorPart part = IDE.openEditor(page, (IFile)res);
				if (offset > -1 && part instanceof ITextEditor) {
					((ITextEditor)part).selectAndReveal(offset, length);
				}
				return true;
			}
			else if (uri.getScheme().equals("http")) {
				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(uri.toURL());
					return true;
				} catch (PartInitException e) {
					Activator.log("Cannot get editor part", e);
				} catch (MalformedURLException e) {
					Activator.log("Cannot resolve link", e);
				}
			}
			else {
				URIStorage storage = new URIStorage(uri);
				IEditorInput input = new URIEditorInput(storage);
				IEditorDescriptor[] ids = PlatformUI.getWorkbench().getEditorRegistry().getEditors(uri.getPath());
				
				if (ids == null || ids.length == 0) {
					ids = new IEditorDescriptor[] { PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor("bla.txt") };
				}
				if (ids != null && ids.length > 0) {
					IEditorPart part = IDE.openEditor(page, input, ids[0].getId(), true);
					if (offset > -1 && part instanceof ITextEditor) {
						((ITextEditor)part).selectAndReveal(offset, length);
					}
					return true;
				}
			}
			
			Activator.log("Can not open link " + uri, null);
		} 
		catch (CoreException e) {
			Activator.log("Can not follow link", e);
			return false;
		} 
		catch (Throw e) {
			Activator.log("Can not follow link", e);
			return false;
		}
		return false;
	}
}
