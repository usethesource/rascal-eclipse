package org.rascalmpl.uri;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.imp.pdb.facts.ISourceLocation;

public interface IURIResourceResolver {
	IResource getResource(ISourceLocation uri) throws IOException;
}
