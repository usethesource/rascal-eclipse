/*******************************************************************************
 * Copyright (c) 2009-2015 CWI
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
package org.rascalmpl.uri;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.values.ValueFactoryFactory;

public class ProjectURIResolver implements ISourceLocationInputOutput, IURIResourceResolver {
	
	public static ISourceLocation constructProjectURI(IProject project, IPath path){
		return constructProjectURI(project.getName(), path);
	}

	private static ISourceLocation constructProjectURI(String project, IPath path){
		try{
			return ValueFactoryFactory.getValueFactory().sourceLocation("project", project, "/" + path.toString());
		}
		catch(URISyntaxException usex){
			throw new BadURIException(usex);
		}
	}
	
	public static ISourceLocation constructProjectURI(IPath workspaceAbsolutePath){
		String projectName        = workspaceAbsolutePath.segment(0);
		IPath projectAbsolutePath = workspaceAbsolutePath.removeFirstSegments(1);
		return constructProjectURI(projectName, projectAbsolutePath);
	}		
	
	@Override
	public InputStream getInputStream(ISourceLocation uri) throws IOException {
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

	public IFile resolveFile(ISourceLocation uri) throws IOException, MalformedURLException {
	    if ("".equals(uri.getAuthority())) {
            throw new IOException("location needs a project name as authority" + uri);
        }
	    
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getAuthority());
		
		if (project == null) {
			throw new IOException("project " + uri.getAuthority() + " does not exist");
		}
		
		return project.getFile(new Path(uri.getPath()));
	}
	
	private IContainer resolveFolder(ISourceLocation uri) throws IOException, MalformedURLException {
	    if ("".equals(uri.getAuthority())) {
	        throw new IOException("location needs a project name as authority" + uri);
	    }
	    
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

	private IResource resolve(ISourceLocation uri) throws IOException, MalformedURLException {
	    if ("".equals(uri.getAuthority())) {
	        throw new IOException("location needs a project name as authority" + uri);
	    }
	    
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getAuthority());
		
		if (project == null || !project.exists()) {
			throw new IOException("project " + uri.getAuthority() + " does not exist.");
		}
		
		if (!project.isOpen()) {
			throw new IOException("project " + uri.getAuthority() + " is closed.");
		}
		
		if(isDirectory(uri)){
			return project.getFolder(uri.getPath());
		}
		
		if(isFile(uri)){
			return project.getFile(uri.getPath());
		}
		
		throw new IOException(uri+" refers to a resource that does not exist.");
	}
	
	public OutputStream getOutputStream(final ISourceLocation uri, boolean append) throws IOException {
		try {
			IFile file = resolveFile(uri);
			
			// this is necessary to also flush possible parent folders to disk first
			if (!file.exists()) {
				file.create(new ByteArrayInputStream(new byte[0]), true, new NullProgressMonitor());
			}
			
			
			// if the above is not done, then the parent folder does not exist.
			return new FileOutputStream(file.getRawLocation().toOSString(), append) {
				@Override
				public void close() throws IOException {
					super.close();
					try {
						file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
					} catch (CoreException e) {
						Activator.getInstance().logException("could not refresh " + uri, e);
					}
				}
			};
		}
		catch (CoreException e) {
			throw new IOException(e);
		}
	}

	public String scheme() {
		return "project";
	}

	@Override
	public boolean exists(ISourceLocation uri) {
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

	@Override
	public boolean isDirectory(ISourceLocation uri) {
		try {
			return resolveFolder(uri).exists();
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	public boolean isFile(ISourceLocation uri) {
		try {
			return resolveFile(uri).exists();
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public long lastModified(ISourceLocation uri) {
		try {
			return resolve(uri).getLocalTimeStamp();
		} catch (MalformedURLException e) {
			return 0L;
		} catch (IOException e) {
			return 0L;
		}
	}

	@Override
	public String[] list(ISourceLocation uri) throws IOException {
		try {
			IContainer folder = resolveFolder(uri);
			IResource[] members = folder.members();
			String[] result = new String[members.length];
			
			for (int i = 0; i < members.length; i++) {
				result[i] = members[i].getName();
			}
			
			return result;
		} catch (CoreException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void remove(ISourceLocation uri) throws IOException {
		try {
			resolve(uri).delete(true, new NullProgressMonitor());
		} catch (CoreException e) {
			throw new IOException("could not remove file", e);
		}
	}

	@Override
	public void mkDirectory(ISourceLocation uri) throws IOException {
		IContainer resolved = resolveFolder(uri);
		NullProgressMonitor pm = new NullProgressMonitor();
		
		if (!resolved.exists()) {
			try { 
				if (resolved instanceof IFolder) {
					((IFolder) resolved).create(false, true, pm);
					resolved.refreshLocal(IResource.DEPTH_ZERO, pm);
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

		return;
	}

	@Override
	public boolean supportsHost() {
		return false;
	}

	@Override
	public Charset getCharset(ISourceLocation uri) throws IOException {
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
	public IResource getResource(ISourceLocation uri) throws IOException {
		return resolve(uri);
	}
}
