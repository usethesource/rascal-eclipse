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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.nature.RascalLibraryFileSystem;
import org.rascalmpl.interpreter.ITestResultListener;
import org.rascalmpl.uri.URIResolverRegistry;

public class TestReporter implements ITestResultListener {
	private final URIResolverRegistry registry;

	public TestReporter(URIResolverRegistry uriResolverRegistry) {
		registry = uriResolverRegistry;
	}

	public void done() {
	}

	public void report(boolean successful, String test, ISourceLocation loc) {
		report(successful, test, loc, null);
	}
	
	public void report(final boolean successful, String test, final ISourceLocation loc, final Throwable t) {
		final IFile file = getFile(loc);
		
		if (file != null) {
			try {
				IMarker m = file.createMarker(IRascalResources.ID_RASCAL_MARKER_TYPE_TEST_RESULTS);
				Map<String,Object> attrs = new HashMap<String,Object>();
				attrs.put(IMarker.TRANSIENT, true);
				attrs.put(IMarker.CHAR_START, loc.getOffset());
				attrs.put(IMarker.CHAR_END, loc.getOffset() + loc.getLength());
				attrs.put(IMarker.MESSAGE, t != null ? t.getMessage() : (successful ? "succeeded" : "failed"));
				attrs.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
				attrs.put(IMarker.SEVERITY, successful ? IMarker.SEVERITY_INFO : IMarker.SEVERITY_ERROR);
				m.setAttributes(attrs);
			} catch (CoreException e) {
				Activator.getInstance().logException(e.getMessage(), e);
			}
		}
	}

	public void start(int count) {

	}
	
	private IFile getFile(ISourceLocation loc) {
		URI uri = loc.getURI();
		String scheme = uri.getScheme();
		
		if (scheme.equals("project")) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getHost());
			
			if (project != null) {
				return project.getFile(uri.getPath());
			}
			
			Activator.getInstance().logException("project " + uri.getHost() + " does not exist", new RuntimeException());
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
				uri = new URI(RascalLibraryFileSystem.SCHEME, RascalLibraryFileSystem.RASCAL, uri.getPath(),"");
				IFile [] files =ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
				if (files.length > 0) {
					return files[0];
				}
				uri = new URI(RascalLibraryFileSystem.SCHEME, RascalLibraryFileSystem.ECLIPSE, uri.getPath(), "");
				files =ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
				if (files.length > 0) {
					return files[0];
				}
			} catch (URISyntaxException e) {
				Activator.getInstance().logException(e.getMessage(), e);
				return null;
			}
		}
	
		
		Activator.getInstance().logException("scheme " + uri.getScheme() + " not supported", new RuntimeException());
		return null;
	}
}
