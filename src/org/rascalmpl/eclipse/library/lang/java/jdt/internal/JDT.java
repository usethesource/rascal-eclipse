/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Bas Basten - Bas.Basten@cwi.nl (CWI)
 *   * Jouke Stoel - Jouke.Stoel@cwi.nl (CWI)
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.library.lang.java.jdt.internal;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.rascalmpl.eclipse.library.util.Resources;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;

public class JDT {
	
	private static final IWorkspaceRoot ROOT = ResourcesPlugin.getWorkspace().getRoot();
    private final IValueFactory VF;
	
    public JDT(IValueFactory vf) {
    	this.VF = vf;
	}
    
    private IProject getProject(String project) {
    	IProject p = ROOT.getProject(project);
		
		if (p == null) {
			throw new Throw(VF.string("Project does not exist"), (ISourceLocation) null, null);
		}
		
		return p;
	}

    private IResource getResource(ISourceLocation loc) {
    	URI uri = loc.getURI();
		
		if (!uri.getScheme().equals("project")) {
			// TODO: This is a terrible error message, since it isn't the case that
			// this scheme isn't supported generally, it just isn't supported in this
			// call. We should give a better message here, like saying we can only get
			// resources that are included inside an Eclipse project.
			throw RuntimeExceptionFactory.schemeNotSupported(loc, null, null);
		}
		
		// ugly workaround b/c URI.getPath() doesn't always return the decoded path
		String path = "";
		try {
			path = URLDecoder.decode(uri.getPath(), java.nio.charset.Charset.defaultCharset().name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (path.length() == 0) {
			throw new Throw(VF.string("URI is not a valid path"), (ISourceLocation) null, null);
		}		

		IProject p = getProject(uri.getHost());
		if (!p.exists(new Path(path))) {
			throw new Throw(VF.string("Path does not exist: " + path), (ISourceLocation) null, null);
		}

		IResource r = p.getFile(path);
		if (!r.exists()) {
			r = p.getFolder(path);
			if (!r.exists()) {
				throw new Throw(VF.string("Path is not a file nor a folder: " + path), (ISourceLocation) null, null);
			}
		}
		
		return r;
    }
    
	public IConstructor extractClass(ISourceLocation loc, IEvaluatorContext eval) {
		IFile file = getIFileForLocation(loc);

		Map<String,IValue> facts = new JDTImporter(eval.getCurrentEnvt().getStore()).importFacts(loc, file);
		IConstructor resource = (IConstructor) Resources.file.make(VF, loc);
		return resource.setAnnotations(facts);
	}
	
	public IValue isOnBuildPath(ISourceLocation loc) {
		IResource r = getResource(loc);
		IJavaProject jp = JavaCore.create(r.getProject());
		
		if (!jp.exists()) {
			throw new Throw(VF.string("Location is not in a Java project: " + loc), (ISourceLocation) null, null);
		}
		
		return VF.bool(jp.isOnClasspath(r));
	}
	
	public IFile getIFileForLocation(ISourceLocation loc) {
		IResource projectRes = getResource(loc);
		if (!(projectRes instanceof IFile)) {
			throw new Throw(VF.string("Location is not a file: " + loc), (ISourceLocation) null, null);
		}
		
		IFile file = (IFile) projectRes;
		if (!file.getFileExtension().equals("java")) {
			throw new Throw(VF.string("Location is not a Java file: " + loc), (ISourceLocation) null, null);
		}
		return file;
	}
	
}
