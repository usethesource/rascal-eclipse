/*******************************************************************************
 * Copyright (c) 2009-2015 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.ValueFactoryFactory;

public class PluginURIResolver extends BundleURIResolver {

	public PluginURIResolver(URIResolverRegistry registry) {
		super(registry);
	}
	
	@Override
	public String scheme() {
		return "plugin";
	}
	
	@Override
	protected ISourceLocation resolve(ISourceLocation uri) throws IOException {
		try {
			String authority = uri.getAuthority();
			
			if (authority == null) {
				throw new IOException("missing authority for bundle name in " + uri);
			}
			
			URL entry = Platform.getBundle(authority).getEntry(uri.getPath());
			if (entry == null) {
				throw new FileNotFoundException(uri.toString());
			}
			
			return super.resolve(ValueFactoryFactory.getValueFactory().sourceLocation(entry.toURI()));
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}
}
