package org.rascalmpl.eclipse.navigator;

import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rascalmpl.interpreter.load.RascalURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;

public class LibraryRootFileStore extends FileStore {
	private final URI uri;
	private final Map<String, IFileStore> map = new HashMap<>();

	public LibraryRootFileStore(URIResolverRegistry reg, URI uri, RascalURIResolver path) {
		this.uri = uri;
		
		for (URI root : path.collect()) {
			map.put(root.toString(), new LibraryFileStore(reg, root, "/"));
		}
	}
	
	@Override
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		Set<String> list = map.keySet();
		String[] result = new String[list.size()];
		
		int i = 0;
		for (String elem : list) {
			result[i++] = elem;
		}
		
		return result;
	}

	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		FileInfo info = new FileInfo(getName());
		info.setDirectory(true);
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
		info.setAttribute(EFS.ATTRIBUTE_IMMUTABLE, true);
		return info;
	}

	@Override
	public IFileStore getChild(String name) {
		return map.get(name);
	}

	@Override
	public String getName() {
		return "search path";
	}

	@Override
	public IFileStore getParent() {
		return null;
	}

	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor)
			throws CoreException {
		return null;
	}

	@Override
	public URI toURI() {
		return uri;
	}
	
}