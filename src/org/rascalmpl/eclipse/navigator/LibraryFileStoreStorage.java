package org.rascalmpl.eclipse.navigator;

import java.io.InputStream;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class LibraryFileStoreStorage implements IStorage {
	private final IFileStore store;

	public LibraryFileStoreStorage(IFileStore store) {
		this.store = store;
	}
	
	public IFileStore getStore() {
		return store;
	}
	
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return null;
	}

	public InputStream getContents() throws CoreException {
		return store.openInputStream(0, new NullProgressMonitor());
	}

	public IPath getFullPath() {
		return new Path(store.toURI().getPath());
	}

	public String getName() {
		return store.getName();
	}

	public boolean isReadOnly() {
		return true;
	}

}
