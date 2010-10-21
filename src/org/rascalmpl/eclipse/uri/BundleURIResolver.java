package org.rascalmpl.eclipse.uri;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.FileLocator;
import org.rascalmpl.uri.IURIInputStreamResolver;
import org.rascalmpl.uri.IURIOutputStreamResolver;
import org.rascalmpl.uri.URIResolverRegistry;

public class BundleURIResolver implements IURIOutputStreamResolver,
		IURIInputStreamResolver {
	private URIResolverRegistry registry;

	public BundleURIResolver(URIResolverRegistry registry) {
		this.registry = registry;
	}
	
	public String absolutePath(URI uri) throws IOException {
		return registry.absolutePath(resolve(uri));
	}

	public OutputStream getOutputStream(URI uri, boolean append)
			throws IOException {
		return registry.getOutputStream(resolve(uri), append);
	}

	public boolean mkDirectory(URI uri) throws IOException {
		return registry.mkDirectory(resolve(uri));
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
			URI result = FileLocator.resolve(uri.toURL()).toURI();
			if (result == uri) {
				throw new IOException("could not resolve " + uri);
			}
			else {
				return result;
			}
		} catch (URISyntaxException e) {
			return null;
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

}
