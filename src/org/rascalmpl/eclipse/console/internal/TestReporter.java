/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.nature.RascalLibraryFileSystem;
import org.rascalmpl.interpreter.ITestResultListener;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;

public class TestReporter implements ITestResultListener {
	private Map<IFile,List<Report>> reports;
	private final URIResolverRegistry resolverRegistry;
	
	private static class Report {
		public boolean successful;
		public String test;
		public ISourceLocation loc;
		public String message;
		public Throwable exception;
		
		public Report(boolean successful, String test, ISourceLocation loc, String message) {
			this.successful = successful;
			this.test = test;
			this.loc = loc;
			this.message = message;
		}
		
	}

	public TestReporter(URIResolverRegistry uriResolverRegistry) {
		resolverRegistry = uriResolverRegistry;
	}

	@Override
	public void done() {
		for (IFile file : reports.keySet()) {
			reportForFile(file, reports.get(file));
		}
		reports = null;
	}
	
	private void reportForFile(final IFile file, final List<Report> list) {
		IWorkspaceRunnable run = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				for (Report r : list) {
					IMarker m = file.createMarker(IRascalResources.ID_RASCAL_MARKER_TYPE_TEST_RESULTS);
					Map<String,Object> attrs = new HashMap<String,Object>();
					attrs.put(IMarker.TRANSIENT, true);
					attrs.put(IMarker.CHAR_START, r.loc.getOffset());
					attrs.put(IMarker.CHAR_END, r.loc.getOffset() + r.loc.getLength());
					attrs.put(IMarker.MESSAGE, r.message);
					attrs.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
					attrs.put(IMarker.SEVERITY, r.successful ? IMarker.SEVERITY_INFO : IMarker.SEVERITY_ERROR);
					m.setAttributes(attrs);
				}
			};
		};
		
		try {
			file.getWorkspace().run(run, file, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		} catch (CoreException e) {
			Activator.getInstance().logException(e.getMessage(), e);
		}
	}

	@Override
	public void report(boolean successful, String test, ISourceLocation loc, String message) {
		final IFile file = getFile(loc);
		
		/*if (loc.getURI().getScheme().equals("rascal")) {
			System.err.println(loc.getURI());
			return;
		}
		*/
		List<Report> forFile = reports.get(file);
		if (forFile == null) {
			forFile = new ArrayList<Report>(1);
			reports.put(file, forFile);
		}
		forFile.add(new Report(successful, test, loc, message));
	}
	

	@Override
	public void start(int count) {
		reports = new HashMap<IFile,List<Report>>();
	}	
	
	private IFile getFile(ISourceLocation loc) {
		return getFile(loc.getURI());
	}
	
	private IFile getFile(URI uri) {
		String scheme = uri.getScheme();
		
		if (scheme.equals("project")) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getAuthority());
			
			if (project != null) {
				return project.getFile(uri.getPath());
			}
			
			Activator.getInstance().logException("project " + uri.getAuthority() + " does not exist", new RuntimeException());
		}
		else if (scheme.equals("file")) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile[] cs = root.findFilesForLocationURI(uri);
			
			if (cs != null && cs.length > 0) {
				return cs[0];
			}
			
			Activator.getInstance().logException("file " + uri + " not found", new RuntimeException());
		}
		else if (scheme.equals(RascalLibraryFileSystem.SCHEME)) {
			IFile [] files =ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
			if (files.length > 0) {
				return files[0];
			}
		}
		else if (scheme.equals("std")) {
			try {
				uri = URIUtil.create(RascalLibraryFileSystem.SCHEME, RascalLibraryFileSystem.RASCAL, uri.getPath());
				IFile [] files =ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
				if (files.length > 0) {
					return files[0];
				}
				uri = URIUtil.create(RascalLibraryFileSystem.SCHEME, RascalLibraryFileSystem.ECLIPSE, uri.getPath());
				files =ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
				if (files.length > 0) {
					return files[0];
				}
			} catch (URISyntaxException e) {
				Activator.getInstance().logException(e.getMessage(), e);
				return null;
			}
		}
		else if (scheme.equals("rascal")) {
			try {
				URI resource = resolverRegistry.getResourceURI(uri);
				return getFile(resource);
			} catch (IOException e) {
				Activator.getInstance().logException("Cannot resolve: " + uri, e);
				e.printStackTrace();
			}
			
		}

		Activator.getInstance().logException("scheme " + uri.getScheme() + " not supported", new RuntimeException());
		return null;
	}


}
