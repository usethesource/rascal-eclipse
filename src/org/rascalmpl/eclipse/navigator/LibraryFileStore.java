package org.rascalmpl.eclipse.navigator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;

public class LibraryFileStore extends FileStore {
	private final LibraryFileStore parent;
	private final URI uri;
	private final String path;
	private final URIResolverRegistry reg;

	public LibraryFileStore(URIResolverRegistry reg, URI project, String path, LibraryFileStore parent) {
		this.reg = reg;
		this.parent = parent;
		this.uri = project;
		this.path = path;
	}

	public LibraryFileStore(URIResolverRegistry reg, URI bundle, String root) {
		this.parent = null;
		this.reg = reg;
		this.uri = bundle;
		this.path = root;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj.getClass() == getClass()) {
			LibraryFileStore other = (LibraryFileStore) obj;
			return uri.equals(other.uri) 
					&& path.equals(other.path);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}


	@Override
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		try {
			return reg.listEntries(getFullPath());
		} catch (IOException e) {
			throw new CoreException(new Status(Status.ERROR, IRascalResources.ID_RASCAL_ECLIPSE_PLUGIN, e.getMessage(), e));
		}
	}

	private URI getFullPath() {
		return URIUtil.getChildURI(uri, path);
	}

	boolean isDirectory(String path) {
		return reg.isDirectory(getFullPath());
	}

	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor)
			throws CoreException {
		FileInfo info = new FileInfo(getName());
		
		if (isDirectory(getFullPath().toString())) {
			info.setDirectory(true);
		} else {
			info.setDirectory(false);
		}

		info.setExists(reg.exists(getFullPath()));
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
		info.setAttribute(EFS.ATTRIBUTE_IMMUTABLE, true);
		return info;
	}

	@Override
	public IFileStore getChild(String name) {
		if (isDirectory(getFullPath().toString())) {
			return new LibraryFileStore(reg, uri, new Path(path).append(name).toString(), this);
		}

		return null;
	}

	@Override
	public String getName() {
		return !isRootPath() ? new Path(path).lastSegment() : computeName(uri);
	}

	private boolean isRootPath() {
		return path == null || path.length() == 0 || "/".equals(path);
	}

	private String computeName(URI uri) {
		return uri.toString();
	}

	@Override
	public IFileStore getParent() {
		return parent;
	}

	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor)
			throws CoreException {
		try {
			return reg.getInputStream(getFullPath());
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, "", e.getMessage()));
		}
	}

	@Override
	public URI toURI() {
		return getFullPath();
	}
}