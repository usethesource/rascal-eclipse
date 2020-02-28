package org.rascalmpl.uri;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.rascalmpl.eclipse.IRascalResources;

import io.usethesource.vallang.ISourceLocation;

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
	
	@SuppressWarnings({"rawtypes", "unchecked"})
    @Override
	public Object getAdapter(Class adapter) {
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
		return new Path("//" + uri.getScheme() + "/" + uri.getAuthority() + uri.getPath());
	}

	public String getName() {
		return URIUtil.getLocationName(uri);
	}

	public boolean isReadOnly() {
		return true;
	}
}
