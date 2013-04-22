package org.rascalmpl.eclipse.uri;

import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.rascalmpl.uri.URIUtil;

public class LibraryURIResolver implements IURIResourceResolver {

	@Override
	public IResource getResource(URI uri) throws IOException {
		if (uri.getScheme().equals("std")) {
			uri = URIUtil.assumeCorrect("rascal-library", "rascal", uri.getPath());
		}
		else if (uri.getScheme().equals("eclipse-std")) {
			uri = URIUtil.assumeCorrect("rascal-library", "eclipse", uri.getPath());
		}
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile[] files = root.findFilesForLocationURI(uri);
		for (IFile f : files) {
			if (f.exists())
				return f; 
		}
		return null;
	}
	

}
