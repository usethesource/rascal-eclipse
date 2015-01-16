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
package org.rascalmpl.eclipse.navigator;

import java.net.URI;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;

public class LibraryFileSystem extends FileSystem {
	public static final String SCHEME = "rascal-path";

	public static LibraryFileSystem getInstance() throws CoreException {
		return (LibraryFileSystem) EFS.getFileSystem(SCHEME);
	}
	
	public LibraryFileSystem() {
		super();
	}

	@Override
	public IFileStore getStore(URI uri) {
		if (!uri.getScheme().equals(SCHEME)) {
			return null;
		} 

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getAuthority());
		
		if (project == null) {
			return null;
		}
		
		Evaluator eval = ProjectEvaluatorFactory.getInstance().getEvaluator(project);
		URIResolverRegistry reg = eval.getResolverRegistry();
		
		if ("/".equals(uri.getPath()) || "".equals(uri.getPath())) {
			return new LibraryRootFileStore(reg, uri, eval.getRascalResolver()); 
		}
		else {
			List<URI> path = eval.getRascalResolver().collect();
			
			for (URI root : path) {
				if (reg.exists(URIUtil.getChildURI(root, uri.getPath()))) {
					return new LibraryFileStore(reg, root, uri.getPath());
				}
			}
			
			return null;
		}
	}

}
