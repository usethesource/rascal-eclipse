package org.rascalmpl.eclipse.uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.rascalmpl.uri.IURIInputStreamResolver;
import org.rascalmpl.uri.IURIOutputStreamResolver;

/**
 * This class provides read/write access to the source code of the rascal plugin 
 * when Rascal is run in a second level Eclipse for bootstrapping purposes.
 */
public class BootstrapURIResolver implements IURIInputStreamResolver,
		IURIOutputStreamResolver {
	private final Bundle bundle = Platform.getBundle("rascal_plugin");

	public boolean exists(URI uri) {
		try {
			return getExistingFile(uri) != null;
		} catch (IOException e) {
			return false;
		}
	}

	private File getExistingFile(URI uri) throws IOException {
		URL url = bundle.getEntry(uri.getPath());
		if (url != null) {
			return new File(FileLocator.toFileURL(url).getFile());
		}
		else {
			throw new FileNotFoundException(uri.toString());
		}
	}
	
	private File getNewFile(URI uri) throws IOException {
		URL url = bundle.getEntry("/");
		return new File(FileLocator.toFileURL(url).getFile(), uri.getPath());
	}

	public InputStream getInputStream(URI uri) throws IOException {
		return new FileInputStream(getExistingFile(uri));
	}

	public boolean isDirectory(URI uri) {
		try {
			return getExistingFile(uri).isDirectory();
		} catch (IOException e) {
			return false;
		}
	}

	public boolean isFile(URI uri) {
		try {
			return getExistingFile(uri).isFile();
		} catch (IOException e) {
			return false;
		}
	}

	public long lastModified(URI uri) throws IOException {
		return getExistingFile(uri).lastModified();
	}

	public String[] listEntries(URI uri) throws IOException {
		return getExistingFile(uri).list(null);
	}

	public String scheme() {
		return "boot";
	}

	public OutputStream getOutputStream(URI uri, boolean append) throws IOException {
		return new FileOutputStream(getNewFile(uri), append);
	}

	public boolean mkDirectory(URI uri) throws IOException {
		return getNewFile(uri).mkdir();
	}

	public String absolutePath(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}

}
