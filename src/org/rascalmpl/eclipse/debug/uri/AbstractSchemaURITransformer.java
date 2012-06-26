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
import java.io.OutputStream;
import java.net.URI;

import org.rascalmpl.uri.IURIOutputStreamResolver;

/**
 * A abstract transformer that converts an {@link URI} from one schema to another.
 * Output stream functionality is not supported.
 */
public abstract class AbstractSchemaURITransformer implements IURIOutputStreamResolver {

	/* (non-Javadoc)
	 * @see org.rascalmpl.uri.IURIOutputStreamResolver#getOutputStream(java.net.URI, boolean)
	 */
	@Override
	public OutputStream getOutputStream(URI uri, boolean append)
			throws IOException {
		throw new UnsupportedOperationException("Not supported by URI transformer.");
	}

	/* (non-Javadoc)
	 * @see org.rascalmpl.uri.IURIOutputStreamResolver#mkDirectory(java.net.URI)
	 */
	@Override
	public void mkDirectory(URI uri) throws IOException {
		throw new UnsupportedOperationException("Not supported by URI transformer.");
	}

}
