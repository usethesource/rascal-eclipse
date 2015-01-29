package org.rascalmpl.eclipse.uri;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.imp.pdb.facts.ISourceLocation;

public class FileURIResourceResolver implements IURIResourceResolver {

	@Override
	public IResource getResource(ISourceLocation uri) throws IOException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile[] cs = root.findFilesForLocationURI(uri.getURI());
		
		if (cs != null && cs.length > 0) {
			return cs[0];
		}
		
		return null;
	}

}
