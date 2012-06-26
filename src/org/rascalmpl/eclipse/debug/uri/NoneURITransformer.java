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

/**
 * Identity transformer that does not apply any transformation. Used in case the
 * source schema equals the target.
 */
public class NoneURITransformer extends AbstractSchemaURITransformer {
	
	private final String schema;
	
	public NoneURITransformer(String schema) {
		this.schema = schema;
	}
	
	/* (non-Javadoc)
	 * @see org.rascalmpl.uri.IURIOutputStreamResolver#scheme()
	 */
	@Override
	public String scheme() {
		return schema;
	}

	/* (non-Javadoc)
	 * @see org.rascalmpl.uri.IURIOutputStreamResolver#getResourceURI(java.net.URI)
	 */
	@Override
	public URI getResourceURI(URI uri) throws IOException {
		return uri;
	}

}
