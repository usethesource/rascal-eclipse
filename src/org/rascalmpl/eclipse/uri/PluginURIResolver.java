package org.rascalmpl.eclipse.uri;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
			return super.resolve(Platform.getBundle(uri.getAuthority()).getEntry(uri.getPath()).toURI());
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}
}
