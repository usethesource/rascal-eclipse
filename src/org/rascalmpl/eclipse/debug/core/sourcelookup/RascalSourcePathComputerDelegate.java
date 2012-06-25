/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.debug.core.sourcelookup;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.rascalmpl.eclipse.IRascalResources;

public class RascalSourcePathComputerDelegate implements ISourcePathComputerDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {

		String path_mainModule = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, (String)null);
		String path_project = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROJECT, (String)null);

		/* 
		 * Retrieving and an associated, if present.
		 */
		IProject associatedProject = null;
		
		if(path_mainModule != null) {
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			associatedProject = root.findMember(path_mainModule).getProject();			
		
		} else if (path_project != null) {
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			associatedProject = root.getProject(path_project);
		
		}
		
		assert associatedProject == null || associatedProject != null;		

	
		/*
		 * Calculating the final set of source containers.
		 */
		if (associatedProject != null) {
			
			/*
			 * Order matters here:
			 * (1) the standard library is searched first;
			 * (2) then all the project and all referenced projects are searched recursively.
			 */
			ISourceContainer[] sourceContainers = new ISourceContainer[] {
				new FolderSourceContainer(associatedProject.getFolder("std"), true),
				new ProjectSourceContainer(associatedProject, true)
			};
		
			return sourceContainers;
		
		} else {
			
			/* default case */
			return new ISourceContainer[]{};			
		
		}
	}
	
}
