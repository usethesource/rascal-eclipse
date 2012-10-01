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

import org.rascalmpl.uri.URIUtil;

/**
 * Transforms an {@link URI} of schema type "std" to "project". Transformations
 * are performed on the basis that the standard library folder is linked as a
 * top level folder within the Eclipse project.
 */
public class StandardLibraryToProjectURITransformer extends AbstractSchemaURITransformer {

	private final String projectName;
	private final String linkedStandardLibraryFolderName;
	
	public StandardLibraryToProjectURITransformer(String projectName, String libraryFolderName) {
		this.projectName = projectName;
		this.linkedStandardLibraryFolderName = libraryFolderName;
	}
	
	/* (non-Javadoc)
	 * @see org.rascalmpl.uri.IURIOutputStreamResolver#scheme()
	 */
	@Override
	public String scheme() {
		return "std";
	}

	/* (non-Javadoc)
	 * @see org.rascalmpl.uri.IURIOutputStreamResolver#getResourceURI(java.net.URI)
	 */
	@Override
	public URI getResourceURI(URI uri) throws IOException {
		return URIUtil.assumeCorrect("project", projectName, "/" + linkedStandardLibraryFolderName + uri.getPath());
	}

}
