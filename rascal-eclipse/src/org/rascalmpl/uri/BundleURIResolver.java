/*******************************************************************************
 * Copyright (c) 2009-2015 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *******************************************************************************/
package org.rascalmpl.uri;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.FileLocator;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.vallang.ISourceLocation;

public class BundleURIResolver implements  ISourceLocationInputOutput {

	@Override
	public OutputStream getOutputStream(ISourceLocation uri, boolean append)
			throws IOException {
		ISourceLocation parent = resolve(URIUtil.getParentLocation(uri));
		// TODO: why twice resolve?
		parent = resolve(parent);
		return URIResolverRegistry.getInstance().getOutputStream(URIUtil.getChildLocation(parent, URIUtil.getLocationName(uri)), append);
	}

	@Override
	public void mkDirectory(ISourceLocation uri) throws IOException {
		ISourceLocation parent = resolve(URIUtil.getParentLocation(uri));
		// TODO: why twice resolve?
		parent = resolve(parent);
		URIResolverRegistry.getInstance().mkDirectory(URIUtil.getChildLocation(parent, URIUtil.getLocationName(uri)));
	}

	@Override
	public void remove(ISourceLocation uri) throws IOException {
	    URIResolverRegistry.getInstance().remove(resolve(uri));
	}

	@Override
	public String scheme() {
		return "bundleresource";
	}

	@Override
	public boolean exists(ISourceLocation uri) {
		try {
			return URIResolverRegistry.getInstance().exists(resolve(uri));
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public InputStream getInputStream(ISourceLocation uri) throws IOException {
		return URIResolverRegistry.getInstance().getInputStream(resolve(uri));
	}

	protected ISourceLocation resolve(ISourceLocation uri) throws IOException {
		try {
			URL url = uri.getURI().toURL();
			URL resolved = FileLocator.resolve(url);
			
			if (resolved == url) {
				throw new IOException("could not resolve " + uri);
			}
			
			if (resolved.getProtocol().equals("jar")) {
				String path = resolved.getPath();
				
				if (path.startsWith("file:")) {
					path = path.substring(5);
				}
				
				// TODO: this does not respect offsets
				return ValueFactoryFactory.getValueFactory().sourceLocation(resolved.getProtocol(), resolved.getAuthority(), path);
			}
			else {
				return ValueFactoryFactory.getValueFactory().sourceLocation(URIUtil.fromURL(resolved));
			}
		} catch (URISyntaxException e) {
			throw new IOException("unexpected URI syntax exception: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean isDirectory(ISourceLocation uri) {
		try {
			return URIResolverRegistry.getInstance().isDirectory(resolve(uri));
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public boolean isFile(ISourceLocation uri) {
		try {
			return URIResolverRegistry.getInstance().isFile(resolve(uri));
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public long lastModified(ISourceLocation uri) throws IOException {
		return URIResolverRegistry.getInstance().lastModified(resolve(uri));
	}

	@Override
	public void setLastModified(ISourceLocation uri, long timestamp) throws IOException {
        URIResolverRegistry.getInstance().setLastModified(resolve(uri), timestamp);
	}
	
	@Override
	public String[] list(ISourceLocation uri) throws IOException {
		return URIResolverRegistry.getInstance().listEntries(resolve(uri));
	}

	@Override
	public boolean supportsHost() {
		return false;
	}

	@Override
	public Charset getCharset(ISourceLocation uri) {
		return Charset.defaultCharset(); // TODO
	}
}
