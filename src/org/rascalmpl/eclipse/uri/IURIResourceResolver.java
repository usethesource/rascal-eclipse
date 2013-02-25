package org.rascalmpl.eclipse.uri;

import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IResource;

public interface IURIResourceResolver {
  IResource getResource(URI uri) throws IOException;
}
