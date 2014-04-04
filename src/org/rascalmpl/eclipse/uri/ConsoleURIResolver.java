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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.rascalmpl.uri.BadURIException;
import org.rascalmpl.uri.IURIInputStreamResolver;
import org.rascalmpl.uri.IURIOutputStreamResolver;
import org.rascalmpl.uri.URIUtil;

public class ConsoleURIResolver implements IURIInputStreamResolver, IURIOutputStreamResolver{
	
	public static URI constructConsoleURI(String id){
		try{
			return URIUtil.create("console", id, ""); 
		}catch(URISyntaxException usex){
			throw new BadURIException(usex);
		}
	}
	
	public ConsoleURIResolver(){
		super();
	}
	
	@Override
	public String scheme(){
		return "console";
	}
	
	@Override
	public InputStream getInputStream(URI uri) throws IOException{
		throw new UnsupportedOperationException("Not supported by console.");
	}

	@Override
	public OutputStream getOutputStream(URI uri, boolean append) throws IOException{
		throw new UnsupportedOperationException("Not supported by console.");
	}
	
	@Override
	public void remove(URI uri) throws IOException {
	  throw new UnsupportedOperationException("Not supported by console.");
	}
	
	@Override
	public boolean exists(URI uri) {
		return true;
	}

	@Override
	public boolean isDirectory(URI uri) {
		return false;
	}

	@Override
	public boolean isFile(URI uri) {
		return false;
	}

	@Override
	public long lastModified(URI uri) {
		return 0L;
	}

	@Override
	public String[] listEntries(URI uri) {
		return new String[0];
	}

	@Override
	public void mkDirectory(URI uri) {
		throw new UnsupportedOperationException("Not supported by console");
	}

	@Override
	public URI getResourceURI(URI uri) {
		return URIUtil.invalidURI();
	}

	@Override
	public boolean supportsHost() {
		return false;
	}

	@Override
	public Charset getCharset(URI uri) {
		throw new UnsupportedOperationException("Not supported by console.");
	}
}
