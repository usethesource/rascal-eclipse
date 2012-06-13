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
package org.rascalmpl.eclipse.launch;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.ConsoleFactory;
import org.rascalmpl.eclipse.console.ConsoleFactory.IRascalConsole;
import org.rascalmpl.eclipse.debug.core.model.RascalDebugTarget;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.Evaluator;

public class LaunchDelegate implements ILaunchConfigurationDelegate{

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// find if there is a rascal file associated to this configuration
		String path_mainModule = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, (String) null);

		// find if there is a rascal project associated to this configuration
		// (corresponds to a console launched from the contextual menu of a project)
		String path_project = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROJECT, (String) null);

		
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
		 * Launching console in either run or debug configuration
		 */
		ConsoleFactory consoleFactory = ConsoleFactory.getInstance();
		IRascalConsole console = null;
		
		if (mode.equals(ILaunchManager.RUN_MODE)) {
	
			if (associatedProject != null) {
				console = consoleFactory.openRunConsole(associatedProject);
			} else {
				console = consoleFactory.openRunConsole();				
			}
			
		} else if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			
			// create a new debug session
			RascalDebugTarget debugTarget = new RascalDebugTarget(launch);

			if (associatedProject != null) {
				console = consoleFactory.openDebuggableConsole(associatedProject, debugTarget.getThread());
			} else {
				console = consoleFactory.openDebuggableConsole(debugTarget.getThread());				
			}			
			
			debugTarget.setConsole(console);
			launch.addDebugTarget(debugTarget);
			
		} else {
			throw new RuntimeException("Unknown or unsupported launch mode: " + mode);
		}
		
		assert console != null;
		
		
		/* 
		 * If a main module is present, import it in the console  evaluator and launch its main() function.
		 * 
		 * TODO: Peform import and main() call asynchronously.
		 * TODO: Make the performed statements plus output visible in the console and its command history.
		 */
		if(path_mainModule != null) {
			
			// construct the corresponding module name
			int index = path_mainModule.indexOf('/', 1);
			String moduleFullName = path_mainModule.substring(index+1);
			if(moduleFullName.startsWith("src/")) {
				moduleFullName = moduleFullName.replaceFirst("src/", "");		
			}
			moduleFullName = moduleFullName.replaceAll("/", "::");
			moduleFullName = moduleFullName.substring(0, moduleFullName.length()-Configuration.RASCAL_FILE_EXT.length());

			// import the main module and launch the main function			
			Evaluator consoleEvaluator = console.getRascalInterpreter().getEval();
			synchronized (consoleEvaluator) {
				consoleEvaluator.doImport(null, moduleFullName);
				consoleEvaluator.eval(null, "main()", URI.create("run:///"));
			}			
		
		}
			
	}
	
}
