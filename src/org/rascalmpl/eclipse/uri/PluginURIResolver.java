package org.rascalmpl.eclipse.uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.rascalmpl.uri.URIResolverRegistry;

public class PluginURIResolver extends BundleURIResolver {

	public PluginURIResolver(URIResolverRegistry registry) {
		super(registry);
	}
	
	@Override
	public String scheme() {
		return "plugin";
	}
	
	@Override
	protected URI resolve(URI uri) throws IOException {
		try {
			String authority = uri.getAuthority();
			
			if (authority == null) {
				throw new IOException("missing authority for bundle name in " + uri);
			}
			
			URL entry = Platform.getBundle(authority).getEntry(uri.getPath());
			if (entry == null) {
				throw new FileNotFoundException(uri.toString());
			}
			return super.resolve(entry.toURI());
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}
}
