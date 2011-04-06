/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
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
import org.rascalmpl.uri.IURIInputOutputResolver;

/**
 * This class provides read/write access to the source code of the rascal plugin 
 * when Rascal is run in a second level Eclipse for bootstrapping purposes.
 */
public class BootstrapURIResolver implements IURIInputOutputResolver {
	private final Bundle bundle = Platform.getBundle("rascal");

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
		throw new FileNotFoundException(uri.toString());
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

	public URI getResourceURI(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}

}
