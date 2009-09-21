package org.meta_environment.rascal.eclipse.console;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.model.ISourceProject;
import org.meta_environment.uri.BadURIException;
import org.meta_environment.uri.IURIInputStreamResolver;
import org.meta_environment.uri.IURIOutputStreamResolver;

public class ProjectURIResolver implements IURIInputStreamResolver, IURIOutputStreamResolver {
	
	public static URI constructProjectURI(ISourceProject project, IPath path){
		try{
			return new URI("project://"+project.getName()+"/"+path);
		}catch(URISyntaxException usex){
			throw new BadURIException(usex);
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
	
	public OutputStream getOutputStream(URI uri) throws IOException {
		return new FileOutputStream(resolve(uri).getRawLocation().toOSString());
	}

	public String scheme() {
		return "project";
	}
}
