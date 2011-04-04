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

import org.rascalmpl.uri.BadURIException;
import org.rascalmpl.uri.IURIInputStreamResolver;
import org.rascalmpl.uri.IURIOutputStreamResolver;

public class ConsoleURIResolver implements IURIInputStreamResolver, IURIOutputStreamResolver{
	
	public static URI constructConsoleURI(String id){
		try{
			return new URI("console://"+id);
		}catch(URISyntaxException usex){
			throw new BadURIException(usex);
		}
	}
	
	public ConsoleURIResolver(){
		super();
	}
	
	public String scheme(){
		return "console";
	}
	
	public InputStream getInputStream(URI uri) throws IOException{
		throw new UnsupportedOperationException("Not supported by console.");
	}

	public OutputStream getOutputStream(URI uri, boolean append) throws IOException{
		throw new UnsupportedOperationException("Not supported by console.");
	}
	
	public boolean exists(URI uri) {
		return true;
	}

	public boolean isDirectory(URI uri) {
		return false;
	}

	public boolean isFile(URI uri) {
		return false;
	}

	public long lastModified(URI uri) {
		return 0L;
	}

	public String[] listEntries(URI uri) {
		// TODO Auto-generated method stub
		String[] ls = {};
		return ls;
	}

	public boolean mkDirectory(URI uri) {
		return false;
	}

	public URI getResourceURI(URI uri) {
		return URI.create("file://-");
	}

}
