package org.rascalmpl.eclipse.uri;

import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.rascalmpl.uri.URIUtil;

public class LibraryURIResolver implements IURIResourceResolver {

	@Override
	public IResource getResource(URI uri, String projectName)
			throws IOException {
		uri = fixRascalURI(uri);
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject proj = projectName == null ? null : root.getProject(projectName);
		IResource nonProjectResult = null;
		if (proj != null) {
			IFile[] files = root.findFilesForLocationURI(uri);
			for (IFile f : files) {
				if (f.exists()) {
					if (proj == null || f.getProject() == proj) {
						return f; 
					}
					else if (nonProjectResult == null) {
						nonProjectResult = f;
					}
				}
			}
		}
		return nonProjectResult;
	}

	private URI fixRascalURI(URI uri) {
		if (uri.getScheme().equals("std")) {
			uri = URIUtil.assumeCorrect("rascal-library", "rascal", uri.getPath());
		}
		else if (uri.getScheme().equals("eclipse-std")) {
			uri = URIUtil.assumeCorrect("rascal-library", "eclipse", uri.getPath());
		}
		return uri;
	}
	

}
