/*******************************************************************************
 * Copyright (c) 2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.rascalmpl.eclipse.IRascalResources;

/**
 * Cache of calcuatable launch configuration properties including convenience methods.
 */
public class LaunchConfigurationPropertyCache {

	private final ILaunchConfiguration configuration;
	
	private final String pathOfMainModule;
	
	private final String pathOfProject;

	private final IProject associatedProject;
	
	/**
	 * Creates a launch configuration property cache. 
	 * 
	 * @param configuration the configuration object associated with a launch
	 */
	public LaunchConfigurationPropertyCache(ILaunchConfiguration configuration) {
		this.configuration = configuration;
	
		String pathOfMainModule = null; 
		String pathOfProject    = null;
		
		try {
			// find if there is a rascal file associated to this configuration
			pathOfMainModule = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, (String) null);
		
			// find if there is a rascal project associated to this configuration
			// (corresponds to a console launched from the contextual menu of a project)
			pathOfProject = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROJECT, (String) null);			
			
		} catch (CoreException e) {
			throw new RuntimeException(e);
		
		} finally {
			this.pathOfMainModule = pathOfMainModule;
			this.pathOfProject    = pathOfProject;
		}

		this.associatedProject = calculateAssociatedProject();
	}
	
	/**
	 * @return if configuration has an Rascal project associated
	 */
	public boolean hasAssociatedProject() {
		return getAssociatedProject() != null;
	}
	
	/**
	 * @return the associatedProject
	 */
	public IProject getAssociatedProject() {
		return associatedProject;
	}	
	
	/**
	 * @return a project if associated with the {@link #configuration}, otherwise <code>null</code>
	 */
	private IProject calculateAssociatedProject() {
		/* 
		 * Retrieving and an associated, if present.
		 */
		IProject associatedProject = null;
		
		if(hasPathOfMainModule()) {
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			associatedProject = root.findMember(getPathOfMainModule()).getProject();			
		
		} else if (hasPathOfProject()) {
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			associatedProject = root.getProject(getPathOfProject());
		
		}
		
		assert associatedProject == null || associatedProject != null;
		return associatedProject;
	}

	/**
	 * @return the configuration
	 */
	public ILaunchConfiguration getConfiguration() {
		return configuration;
	}
			
	/**
	 * @return if configuration has a main module set
	 */
	public boolean hasPathOfMainModule() {
		return pathOfMainModule != null;
	}
	
	/**
	 * @return the pathOfMainModule
	 */
	public String getPathOfMainModule() {
		return pathOfMainModule;
	}

	/**
	 * @return if configuration has a project set
	 */
	public boolean hasPathOfProject() {
		return pathOfProject != null;
	}
	
	/**
	 * @return the pathOfProject
	 */
	public String getPathOfProject() {
		return pathOfProject;
	}

}