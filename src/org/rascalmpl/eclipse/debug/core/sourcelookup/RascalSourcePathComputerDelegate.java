/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.debug.core.sourcelookup;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.rascalmpl.eclipse.launch.LaunchConfigurationPropertyCache;
import org.rascalmpl.uri.URIUtil;

public class RascalSourcePathComputerDelegate implements ISourcePathComputerDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
	
		LaunchConfigurationPropertyCache configurationUtility = new LaunchConfigurationPropertyCache(configuration);
				
		/*
		 * Calculating the final set of source containers.
		 */
		if (configurationUtility.hasAssociatedProject()) {
			
			IProject associatedProject = configurationUtility.getAssociatedProject();
			
			IFileStore libraryStore = EFS.getStore(URIUtil.assumeCorrect("rascal-library","rascal",""));
			IFileStore eclipseLibraryStore = EFS.getStore(URIUtil.assumeCorrect("rascal-library", "eclipse", ""));
			
			ISourceContainer[] sourceContainers = new ISourceContainer[] {
				new ProjectSourceContainer(associatedProject, true),
				new DirectorySourceContainer(libraryStore.toLocalFile(EFS.NONE, monitor), true),
				new DirectorySourceContainer(eclipseLibraryStore.toLocalFile(EFS.NONE, monitor), true),
			};
		
			return sourceContainers;
		
		} else {
			
			/* default case */
			return new ISourceContainer[]{};			
		
		}
	}
	
}
