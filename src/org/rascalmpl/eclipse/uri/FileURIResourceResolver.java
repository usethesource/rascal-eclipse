package org.rascalmpl.eclipse.uri;

import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

public class FileURIResourceResolver implements IURIResourceResolver {

	@Override
	public IResource getResource(URI uri) throws IOException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile[] cs = root.findFilesForLocationURI(uri);
		
		if (cs != null && cs.length > 0) {
			return cs[0];
		}
		
		return null;
	}

}
