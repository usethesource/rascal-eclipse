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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.CompositeSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.rascalmpl.eclipse.launch.LaunchConfigurationPropertyCache;
import org.rascalmpl.eclipse.navigator.RascalLibraryFileSystem;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;

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
			return new ISourceContainer[] { new ProjectSourceContainer(associatedProject, true) };
			
			// TODO: add code to make lookup in the libraries possible
//    RascalLibraryFileSystem fileSystem = RascalLibraryFileSystem.getInstance();
//    Map<String,IFileStore> roots = fileSystem.getRoots();

//			for (IFileStore lib : roots.values()) {
//        sourceContainers[i++] = new FileStoreSourceContainer(lib, true);
//			}
//		
		} else {
			/* default case */
			return new ISourceContainer[]{};			
		}
	}
}
