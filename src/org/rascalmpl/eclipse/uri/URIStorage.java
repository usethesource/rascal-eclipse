package org.rascalmpl.eclipse.uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;

public class URIStorage implements IStorage {
	private final URI uri;
	private final URIResolverRegistry reg;
	private boolean isRoot;

	public URIStorage(URIResolverRegistry reg, URI store, boolean isRoot) {
		this.uri = store;
		this.reg = reg;
		this.isRoot = isRoot;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof URIStorage) {
			return ((URIStorage) obj).uri.equals(uri);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return uri.hashCode();
	}
	
	public boolean isRoot() {
		return isRoot;
	}
	
	public URI getURI() {
		return uri;
	}
	
	public URIResolverRegistry getRegistry() {
		return reg;
	}
	
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

	public InputStream getContents() throws CoreException {
		try {
			return reg.getInputStream(uri);
		} catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, IRascalResources.ID_RASCAL_ECLIPSE_PLUGIN, e.getMessage(), e));
		}
	}

	public IPath getFullPath() {
		return new Path(uri.getPath());
	}

	public String getName() {
		return URIUtil.getURIName(uri);
	}
	
	public boolean exists() {
		return reg.exists(uri);
	}
	
	public URIStorage makeChild(String child) {
		return new URIStorage(reg, URIUtil.getChildURI(uri, child), false);
	}

	public boolean isReadOnly() {
		return true;
	}

	public String[] listEntries() {
		try {
			return reg.listEntries(uri);
		} catch (IOException e) {
			return new String[0];
		}
	}
	
	public boolean isDirectory() {
		return reg.isDirectory(uri);
	}
}
