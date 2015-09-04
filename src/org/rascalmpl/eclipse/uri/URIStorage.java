package org.rascalmpl.eclipse.uri;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;

public class URIStorage implements IStorage {
	private final ISourceLocation uri;

	public URIStorage(ISourceLocation store) {
		this.uri = store;
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
	
	public ISourceLocation getLocation() {
		return uri;
	}
	
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	public InputStream getContents() throws CoreException {
		try {
			return URIResolverRegistry.getInstance().getInputStream(uri);
		} catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, IRascalResources.ID_RASCAL_ECLIPSE_PLUGIN, e.getMessage(), e));
		}
	}

	public IPath getFullPath() {
		return new Path(uri.getPath());
	}

	public String getName() {
		return URIUtil.getLocationName(uri);
	}

	public boolean isReadOnly() {
		return true;
	}
}
