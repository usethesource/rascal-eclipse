package org.meta_environment.rascal.eclipse.nature;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.meta_environment.rascal.eclipse.Activator;
import org.meta_environment.rascal.interpreter.Evaluator;

public class RascalLibraryFileSystem extends FileSystem {
	private static final String SCHEME = "rascal-library";
	public static final String ECLIPSE = "eclipse";
	public static final String RASCAL = "rascal";
	protected Map<String, URI> roots = new HashMap<String, URI>();

	public class RascalLibraryFileStore extends FileStore {
		private File file;
		private File rootFile;
		private URI uri;
		private RascalLibraryFileStore parent;

		public RascalLibraryFileStore(URI uri, RascalLibraryFileStore parent) {
			this(uri);
			this.parent = parent;
		}
		
		public RascalLibraryFileStore(URI uri) {
			this.uri = uri;
			URI root = roots.get(uri.getHost());
			
			if (root == null) {
				Activator.getInstance().logException("could not find library", null);
			}
			
			try {
				rootFile = EFS.getStore(root).toLocalFile(0, null);
				file = new File(rootFile, uri.getPath());
			} catch (CoreException e) {
				Activator.getInstance().logException("could not find file", e);
			}
		}

		public String[] childNames(int options, IProgressMonitor monitor)
				throws CoreException {
			if (file.isDirectory()) {
				List<String> list = new LinkedList<String>();
				
				for (File f : file.listFiles()) {
					if (f.isDirectory() || f.getName().endsWith(".rsc")) {
						list.add(f.getName());
					}
				}
				
				return list.toArray(new String[list.size()]);
			}
			
			return new String[0];
		}

//		@Override
//		public IFileInfo[] childInfos(int options, IProgressMonitor monitor)
//				throws CoreException {
//			if (file.isDirectory()) {
//				List<IFileInfo> list = new LinkedList<IFileInfo>();
//				
//				for (File f : file.listFiles()) {
//					FileInfo info = new FileInfo(getName());
//					if (f.isDirectory()) {
//						info.setDirectory(true);
//					}
//					info.setExists(true);
//					list.add(info);
//				}
//				
//				return list.toArray(new IFileInfo[list.size()]);
//			}
//			
//			return new IFileInfo[0];
//		
//		}
		
		@Override
		public IFileInfo fetchInfo(int options, IProgressMonitor monitor)
				throws CoreException {
			FileInfo info = new FileInfo(getName());
			if (file.isDirectory()) {
				info.setDirectory(true);
				info.setLastModified(file.lastModified());
			} else {
				info.setDirectory(false);
				info.setLastModified(file.lastModified());
			}
			info.setExists(true);
			info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, true);
			return info;
		}
		
		@Override
		public IFileStore getChild(String name) {
			if (file.isDirectory()) {
				try {
					String path = uri.getPath();
					if (!path.endsWith("/") && !name.startsWith("/")) {
						path = path + "/";
					}
					return new RascalLibraryFileStore(new URI(SCHEME, uri.getHost(), path + name, null), this);
				} catch (URISyntaxException e) {
					Activator.getInstance().logException(e.getMessage(), e);
				}
			}
			
			return null;
		}

		@Override
		public String getName() {
			return file.getName();
		}

		@Override
		public IFileStore getParent() {
			return parent;
		}

		@Override
		public InputStream openInputStream(int options, IProgressMonitor monitor)
				throws CoreException {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				throw new CoreException(new Status(IStatus.ERROR, "", e.getMessage()));
			}
		}

		@Override
		public URI toURI() {
			return uri;
		}

	}

	public RascalLibraryFileSystem() {
		addRoot(RASCAL, Evaluator.class, "/StandardLibrary");
		addRoot(ECLIPSE, Activator.class, "/org/meta_environment/rascal/eclipse/lib");
	}

	@Override
	public IFileStore getStore(URI uri) {
		if (!uri.getScheme().equals(SCHEME)) {
			return null;
		}
		
		return new RascalLibraryFileStore(uri);
	}
	
	private void addRoot(String name, Class<?> root, String loc) {
		try {
			URL resource = root.getResource(loc);

			if (resource == null) {
				Activator.getInstance().logException("can not find " + loc + " using " + root.getName(), new NullPointerException());
				return;
			}
			roots.put(name, FileLocator.toFileURL(resource).toURI());
		} catch (URISyntaxException e) {
			Activator.getInstance().logException("linking library failed", e);
		} catch (IOException e) {
			Activator.getInstance().logException("linking library failed", e);
		}
	}

}
