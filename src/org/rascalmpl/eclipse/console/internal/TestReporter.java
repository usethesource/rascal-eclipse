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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.uri.URIResourceResolver;
import org.rascalmpl.interpreter.ITestResultListener;

public class TestReporter implements ITestResultListener {
	private Map<IFile,List<Report>> reports;
	
	private static class Report {
		public boolean successful;
		public ISourceLocation loc;
		public String message;
		
		public Report(boolean successful, String test, ISourceLocation loc, String message, Throwable exception) {
			this.successful = successful;
			this.loc = loc;
			this.message = message;
		}
		
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
			}
		};
		
		try {
			file.getWorkspace().run(run, file, IWorkspace.AVOID_UPDATE, new NullProgressMonitor());
		} catch (CoreException e) {
			Activator.getInstance().logException(e.getMessage(), e);
		}
	}

	@Override
	public void report(boolean successful, String test, ISourceLocation loc, String message, Throwable t) {
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
		forFile.add(new Report(successful, test, loc, message, t));
	}
	

	@Override
	public void start(int count) {
		reports = new HashMap<IFile,List<Report>>();
	}	
	
	private IFile getFile(ISourceLocation uri) {
		IResource res = URIResourceResolver.getResource(uri);
		
		if (res instanceof IFile) {
			return (IFile) res;
		}

		Activator.getInstance().logException("scheme " + uri.getScheme() + " not supported", new RuntimeException());
		return null;
	}
}
