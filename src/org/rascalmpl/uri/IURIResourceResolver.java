package org.rascalmpl.uri;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.rascalmpl.value.ISourceLocation;

public interface IURIResourceResolver {
	IResource getResource(ISourceLocation uri) throws IOException;
}
