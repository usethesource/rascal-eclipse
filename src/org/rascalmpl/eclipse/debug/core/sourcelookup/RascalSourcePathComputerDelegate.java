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

import java.util.Arrays;
import java.util.LinkedList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.rascalmpl.eclipse.IRascalResources;

public class RascalSourcePathComputerDelegate implements ISourcePathComputerDelegate{

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {

		String mainModule = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, (String)null);
		String mainProject = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROJECT, (String)null);

		LinkedList<ISourceContainer> sourceContainers = new  LinkedList<ISourceContainer>();

		if (mainProject != null) {
			//when a project is specified, lookup in the project and its referenced projects
			LinkedList<IProject> projects = new LinkedList<IProject>();
			projects.add(ResourcesPlugin.getWorkspace().getRoot().getProject(mainProject));

			while (! projects.isEmpty()) {
				IProject project = projects.removeFirst();
				sourceContainers.add(new ProjectSourceContainer(project, false));
				projects.addAll(Arrays.asList(project.getDescription().getReferencedProjects()));
			}
			return sourceContainers.toArray(new ISourceContainer[]{});
		}	

		if (mainModule != null) {
			//when a specific file is specified, lookup in its corresponding project (+ referenced projects)
			//when a project is specified, lookup in the project and its referenced projects
			LinkedList<IProject> projects = new LinkedList<IProject>();
			projects.add(ResourcesPlugin.getWorkspace().getRoot().findMember(mainModule).getProject());

			while (! projects.isEmpty()) {
				IProject project = projects.removeFirst();
				sourceContainers.add(new ProjectSourceContainer(project, false));
				projects.addAll(Arrays.asList(project.getDescription().getReferencedProjects()));
			}
			return sourceContainers.toArray(new ISourceContainer[]{});
			}
		
		//TODO: we need to find a way to also add standard library's modules
		
		/* default case */
		return new ISourceContainer[]{};
	}
}
