/*******************************************************************************
 * Copyright (c) 2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.debug.uri;

import java.io.IOException;
import java.net.URI;

import org.eclipse.core.runtime.IPath;

/**
 * Transforms an {@link URI} of schema type "project" to "std" iff the URI links
 * to a standard library file through the Rascal-project-specific linked
 * standard library folder. Transformations are performed on the basis that the
 * standard library folder is linked as a top level folder within the Eclipse
 * project.
 */
public class ProjectURIToStandardLibraryTransformer extends AbstractSchemaURITransformer {

	private final String linkedStandardLibraryFolderName;
	
	public ProjectURIToStandardLibraryTransformer(String libraryFolderName) {
		this.linkedStandardLibraryFolderName = libraryFolderName;
	}
	
	/* (non-Javadoc)
	 * @see org.rascalmpl.uri.IURIOutputStreamResolver#scheme()
	 */
	@Override
	public String scheme() {
		return "project";
	}

	/* (non-Javadoc)
	 * @see org.rascalmpl.uri.IURIOutputStreamResolver#getResourceURI(java.net.URI)
	 */
	@Override
	public URI getResourceURI(URI uri) throws IOException {
		String prefix = IPath.SEPARATOR + linkedStandardLibraryFolderName;
		
		if (uri.getPath().startsWith(prefix)) {		
			String uriString = "std://" + uri.getPath().substring(prefix.length());
			return URI.create(uriString);
		} else {
			return uri;
		}
	}

}
