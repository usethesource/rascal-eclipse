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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.resources.IContainer;
import java.nio.charset.Charset;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.model.ISourceProject;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.uri.BadURIException;
import org.rascalmpl.uri.IURIInputStreamResolver;
import org.rascalmpl.uri.IURIOutputStreamResolver;
import org.rascalmpl.uri.URIUtil;

public class ProjectURIResolver implements IURIInputStreamResolver, IURIOutputStreamResolver, IURIResourceResolver {
	
	public static URI constructProjectURI(ISourceProject project, IPath path){
		return constructProjectURI(project.getName(), path);
	}

	private static URI constructProjectURI(String project, IPath path){
		try{
			return URIUtil.create("project", project, "/" + path.toString());
		}catch(URISyntaxException usex){
			throw new BadURIException(usex);
		}
	}
	
	public static URI constructProjectURI(IPath workspaceAbsolutePath){
		String projectName        = workspaceAbsolutePath.segment(0);
		IPath projectAbsolutePath = workspaceAbsolutePath.removeFirstSegments(1);
		return constructProjectURI(projectName, projectAbsolutePath);
	}		
	
	public InputStream getInputStream(URI uri) throws IOException {
		try {
			return resolveFile(uri).getContents();
		} catch (CoreException e) {
			Throwable cause = e.getCause();
			
			if (cause instanceof IOException) {
				throw (IOException) cause;
			}
			
			throw new IOException(e.getMessage());
		}
	}

	public IFile resolveFile(URI uri) throws IOException, MalformedURLException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getAuthority());
		
		if (project == null) {
			throw new IOException("project " + uri.getAuthority() + " does not exist");
		}
		
		return project.getFile(uri.getPath());
	}
	
	private IContainer resolveFolder(URI uri) throws IOException, MalformedURLException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getAuthority());
		if (project == null) {
			throw new IOException("project " + uri.getAuthority() + " does not exist");
		}
		
		if (uri.getPath().isEmpty() || uri.getPath().equals("/")) {
		  return project;
		}
		else {
		  return project.getFolder(uri.getPath());
		}
	}

	private IResource resolve(URI uri) throws IOException, MalformedURLException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getAuthority());
		
		if (project == null) {
			throw new IOException("project " + uri.getAuthority() + " does not exist");
		}
		
		if(isDirectory(uri)){
			return project.getFolder(uri.getPath());
		}
		
		if(isFile(uri)){
			return project.getFile(uri.getPath());
		}
		
		throw new IOException(uri+" refers to a resource that does not exist.");
	}
	
	public OutputStream getOutputStream(final URI uri, boolean append) throws IOException {
		return new FileOutputStream(resolveFile(uri).getRawLocation().toOSString(), append) {
			@Override
			public void close() throws IOException {
				super.close();
				try {
					resolveFile(uri).refreshLocal(0, new NullProgressMonitor());
				} catch (CoreException e) {
					Activator.getInstance().logException("could not refresh " + uri, e);
				}
			}
		};
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
		} catch (AssertionFailedException e) {
			return false;
		}
	
	}

	public boolean isDirectory(URI uri) {
		try {
			return resolveFolder(uri).exists();
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean isFile(URI uri) {
		try {
			return resolveFile(uri).exists();
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
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
		try {
			IContainer folder = resolveFolder(uri);
			IResource[] members = folder.members();
			String[] result = new String[members.length];
			
			for (int i = 0; i < members.length; i++) {
				result[i] = members[i].getName();
			}
			
			return result;
		} catch (CoreException e) {
			return new String[0];
		} catch (MalformedURLException e) {
			return new String[0];
		} catch (IOException e) {
			return new String[0];
		}
	}

	public void mkDirectory(URI uri) throws IOException {
		IContainer resolved = resolveFolder(uri);
		NullProgressMonitor pm = new NullProgressMonitor();
		
		if (!resolved.exists()) {
			try { 
			  if (resolved instanceof IFolder) {
			    ((IFolder) resolved).create(true, true, pm);
			    resolved.refreshLocal(0, pm);
			  }
			  else if (resolved instanceof IProject) {
			    IProject project = (IProject) resolved;
          project.create(pm);
			    project.open(pm);
			  }
				return;
			} catch (CoreException e) {
				throw new IOException(e.getMessage(), e);
			}
		}

		throw new FileNotFoundException(uri.toString());
	}

	public URI getResourceURI(URI uri) throws IOException {
		try {
			return resolve(uri).getLocation().toFile().toURI();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public boolean supportsHost() {
		return false;
	}

	@Override
	public Charset getCharset(URI uri) throws IOException {
		IFile file;
		try {
			file = resolveFile(uri);
		} catch (MalformedURLException e) {
			return null;
		}
		if (file != null) {
			try {
				String charsetName = file.getCharset();
				if (charsetName != null) 
					return Charset.forName(charsetName);
			} catch (CoreException e) {
				return null;
			}
		}
		return null;
	}

	@Override
	public IResource getResource(URI uri, String projectName) throws IOException {
		return resolve(uri);
	}
}
