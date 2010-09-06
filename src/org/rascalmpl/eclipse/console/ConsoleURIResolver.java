package org.rascalmpl.eclipse.console;

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

}
