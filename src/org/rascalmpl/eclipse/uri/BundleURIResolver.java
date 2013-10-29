/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.uri;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.FileLocator;
import org.rascalmpl.uri.IURIInputStreamResolver;
import org.rascalmpl.uri.IURIOutputStreamResolver;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;

public class BundleURIResolver implements IURIOutputStreamResolver,
		IURIInputStreamResolver {
	private URIResolverRegistry registry;

	public BundleURIResolver(URIResolverRegistry registry) {
		this.registry = registry;
	}
	
	public URI getResourceURI(URI uri) throws IOException {
		return resolve(uri);
	}

	public OutputStream getOutputStream(URI uri, boolean append)
			throws IOException {
		URI parent = resolve(URIUtil.getParentURI(uri));
		parent = resolve(parent);
		parent = fixCoursesSrcDir(parent);
		return registry.getOutputStream(URIUtil.getChildURI(parent, URIUtil.getURIName(uri)), append);
	}

	private URI fixCoursesSrcDir(URI parent) {
	  // TODO: this is a temporary workaround for a problem that we have no good solution for at the moment
	  try {
	    String path = parent.getPath();
	    if (path != null) {
	      path = path.replaceAll("bin/org/rascalmpl/courses", "src/org/rascalmpl/courses");
	    }

	    return URIUtil.changePath(parent, path);
	  } catch (URISyntaxException e) {
	    assert false;
	    return parent;
	  }
  }

  public void mkDirectory(URI uri) throws IOException {
		URI parent = resolve(URIUtil.getParentURI(uri));
		parent = resolve(parent);
		
		registry.mkDirectory(URIUtil.getChildURI(parent, URIUtil.getURIName(uri)));
	}

	public String scheme() {
		return "bundleresource";
	}

	public boolean exists(URI uri) {
		try {
			return registry.exists(resolve(uri));
		} catch (IOException e) {
			return false;
		}
	}

	public InputStream getInputStream(URI uri) throws IOException {
		return registry.getInputStream(resolve(uri));
	}

	private URI resolve(URI uri) throws IOException {
		try {
			URL resolved = FileLocator.resolve(uri.toURL());
			URI result = null;
			try {
				result = URIUtil.fixUnicode(resolved.toURI()); 
			}
			catch (URISyntaxException e) {
				// lets try to make a URI out of the URL.
				String path = resolved.getPath();
				if (path.startsWith("file:")) {
					path = path.substring(5);
				}
				result = URIUtil.create(resolved.getProtocol(), resolved.getAuthority(), path);
			}
			if (result == uri) {
				throw new IOException("could not resolve " + uri);
			}
			
			return result;
		} catch (URISyntaxException e) {
			throw new IOException("unexpected URI syntax exception: " + e.getMessage(), e);
		}
	}

	public boolean isDirectory(URI uri) {
		try {
			return registry.isDirectory(resolve(uri));
		} catch (IOException e) {
			return false;
		}
	}

	public boolean isFile(URI uri) {
		try {
			return registry.isFile(resolve(uri));
		} catch (IOException e) {
			return false;
		}
	}

	public long lastModified(URI uri) throws IOException {
		return registry.lastModified(resolve(uri));
	}

	public String[] listEntries(URI uri) throws IOException {
		return registry.listEntries(resolve(uri));
	}

	@Override
	public boolean supportsHost() {
		return false;
	}

	@Override
	public Charset getCharset(URI uri) {
		// TODO need to check if a JAR actually stores the charset
		return null;
	}

}
