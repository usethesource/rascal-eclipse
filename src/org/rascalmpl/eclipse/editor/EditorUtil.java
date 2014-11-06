package org.rascalmpl.eclipse.editor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.uri.URIResourceResolver;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.control_exceptions.Throw;

public class EditorUtil {
	
	public static boolean openAndSelectURI(ISourceLocation loc, IEvaluatorContext eval) {
		return openAndSelectURI(loc, eval, null);
	}
	public static boolean openAndSelectURI(ISourceLocation loc, IEvaluatorContext eval, String projectName) {
		if (loc.hasOffsetLength()) {
			return openAndSelectURI(loc.getURI(), loc.getOffset(), loc.getLength(), eval, projectName);
		}
		return openAndSelectURI(loc.getURI(), eval, projectName);
	}

	public static boolean openAndSelectURI(URI uri, IEvaluatorContext eval) {
		return openAndSelectURI(uri, eval, null);
	}
	
	public static boolean openAndSelectURI(URI uri, IEvaluatorContext eval, String projectName ) {
		return openAndSelectURI(uri, -1, 0, eval, projectName);
	}
	public static boolean openAndSelectURI(URI uri, int offset, int length, IEvaluatorContext eval) {
		return openAndSelectURI(uri, offset, length, eval, null);
	}

	public static boolean openAndSelectURI(URI uri, int offset, int length, IEvaluatorContext eval, String projectName ) {
		try {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			IResource res = URIResourceResolver.getResource(uri, projectName);
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
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(uri);

				if (fileStore.fetchInfo().exists()) {
					IDE.openEditorOnFileStore( page, fileStore );
					return true;
				}
				else {
					if (projectName != null) {
						IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
						eval = ProjectEvaluatorFactory.getInstance().getEvaluator(project);
					}

					if (eval != null) {
						URI resourceURI = eval.getResolverRegistry().getResourceURI(uri);

						if (resourceURI.getScheme().equals("project")) {
							IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(resourceURI.getAuthority());
							IFile file = project.getFile(resourceURI.getPath());
							IEditorPart part = IDE.openEditor(page, file);
							if (offset > -1 && part instanceof ITextEditor) {
								((ITextEditor)part).selectAndReveal(offset, length);
							}
							return true;
						}

						if (resourceURI != null) {
							URL find = FileLocator.resolve(resourceURI.toURL());
							fileStore = EFS.getLocalFileSystem().getStore(find.toURI());

							if (fileStore != null && fileStore.fetchInfo().exists()) {
								IEditorPart part = IDE.openEditorOnFileStore( page, fileStore );
								if (offset > -1 && part instanceof ITextEditor) {
									((ITextEditor)part).selectAndReveal(offset, length);
								}
								return true;
							}
						}
					}
				}

				Activator.log("can not open link", null);
			}
		} 
		catch (IOException | CoreException e) {
			Activator.log("Cannot follow link", e);
			return false;
		} 
		catch (URISyntaxException e) {
			Activator.log("Cannot follow link", e);
			return false;
		} 
		catch (Throw e) {
			Activator.log("Cannot follow link", e);
			return false;
		}
		return false;
	}
}
