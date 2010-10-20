package org.rascalmpl.eclipse.uri;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.model.ISourceProject;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.uri.BadURIException;
import org.rascalmpl.uri.IURIInputStreamResolver;
import org.rascalmpl.uri.IURIOutputStreamResolver;

public class ProjectURIResolver implements IURIInputStreamResolver, IURIOutputStreamResolver {
	
	public static URI constructProjectURI(ISourceProject project, IPath path){
		try{
			// making sure that spaces in 'path' are properly escaped
			return new URI("project://"+project.getName()+"/"+URLEncoder.encode(path.toOSString(),"UTF8"));
		}catch(URISyntaxException usex){
			throw new BadURIException(usex);
		} catch (UnsupportedEncodingException e) {
			Activator.getInstance().logException(e.getMessage(), e);
			return null;
		}
	}

	public InputStream getInputStream(URI uri) throws IOException {
		try {
			return resolve(uri).getContents();
		} catch (CoreException e) {
			Throwable cause = e.getCause();
			
			if (cause instanceof IOException) {
				throw (IOException) cause;
			}
			
			throw new IOException(e.getMessage());
		}
	}

	private IFile resolve(URI uri) throws IOException, MalformedURLException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getHost());
		
		if (project == null) {
			throw new IOException("project " + uri.getHost() + " does not exist");
		}
		
		return project.getFile(uri.getPath());
	}
	
	public OutputStream getOutputStream(URI uri, boolean append) throws IOException {
		return new FileOutputStream(resolve(uri).getRawLocation().toOSString(), append);
	}

	public String scheme() {
		return "project";
	}

	public boolean exists(URI uri) {
		try {
			return resolve(uri).exists();
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean isDirectory(URI uri) {
		return false;
	}

	public boolean isFile(URI uri) {
		return exists(uri);
	}

	public long lastModified(URI uri) {
		try {
			return resolve(uri).getModificationStamp();
		} catch (MalformedURLException e) {
			return 0L;
		} catch (IOException e) {
			return 0L;
		}
	}

	public String[] listEntries(URI uri) {
		String [] ls = {};
		return ls;
	}

	public boolean mkDirectory(URI uri) {
		return false;
	}

	public String absolutePath(URI uri) throws IOException {
		try {
			return resolve(uri).getFullPath().toOSString();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
