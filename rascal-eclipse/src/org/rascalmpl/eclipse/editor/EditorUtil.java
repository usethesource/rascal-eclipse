package org.rascalmpl.eclipse.editor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.navigator.RascalNavigator;
import org.rascalmpl.exceptions.Throw;
import org.rascalmpl.uri.URIEditorInput;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIResourceResolver;
import org.rascalmpl.uri.URIStorage;

import io.usethesource.vallang.ISourceLocation;

public class EditorUtil {
	
    /**
     * MUST be called from a UI Thread
     */
	public static boolean openAndSelectURI(ISourceLocation uri) {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			URIResolverRegistry reg = URIResolverRegistry.getInstance();
			
			try {
				uri = reg.logicalToPhysical(uri);
			} catch (IOException e) {
				// in case file not found logically
			}
			
			if (reg.isDirectory(uri)) {
				IWorkbenchWindow wbw = Activator.getInstance().getWorkbench().getActiveWorkbenchWindow();
				RascalNavigator nav = (RascalNavigator) wbw.getActivePage().findView("rascal.navigator");
				if (nav != null) {
					nav.reveal(uri);
				}
				
				return true;
			}
			
			IResource res = URIResourceResolver.getResource(uri);
			if (res != null && res instanceof IFile) {
				IEditorPart part = IDE.openEditor(page, (IFile)res);
				
				if (uri.hasOffsetLength() && part instanceof ITextEditor) {
					((ITextEditor)part).selectAndReveal(uri.getOffset(), uri.getLength());
				}
				return true;
			}
			else if (uri.getScheme().equals("http")) {
				try {
					PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(uri.getURI().toURL());
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
					if (uri.hasOffsetLength() && part instanceof ITextEditor) {
						((ITextEditor)part).selectAndReveal(uri.getOffset(), uri.getLength());
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

  /**
   * MUST be called from a UI thread
   * @param loc
   */
  public static void openWebURI(ISourceLocation loc) {
    try {
      String link = loc.getURI().toString();

      PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(link));
    }
    catch (PartInitException e) {
      Activator.log("Couldn't open weblink", e);
    }
    catch (MalformedURLException e) {
      Activator.log("Couldn't open weblink", e);
    }
  }
}
