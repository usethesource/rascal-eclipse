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
package org.rascalmpl.uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.rascalmpl.eclipse.nature.BundleClassLoader;
import org.rascalmpl.uri.classloaders.IClassloaderLocationResolver;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.values.ValueFactoryFactory;

public class PluginURIResolver extends BundleURIResolver implements IClassloaderLocationResolver {
 
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

    @Override
    public ClassLoader getClassLoader(ISourceLocation loc, ClassLoader parent) throws IOException {
        Bundle bundle = Platform.getBundle(loc.getAuthority());
        
        if (bundle == null) {
            throw new IOException("Bundle " + loc + " not found");
        }
        
        // note how the parent parameter is ignored here to make sure bundles
        // do not interact with each other
        return new BundleClassLoader(bundle);
    }
}
