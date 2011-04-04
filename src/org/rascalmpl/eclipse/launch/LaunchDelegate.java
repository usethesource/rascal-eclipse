/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - emilie.balland@inria.fr (INRIA)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *******************************************************************************/
package org.rascalmpl.eclipse.launch;

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

// TODO Tidy up here.
public class LaunchDelegate implements ILaunchConfigurationDelegate{

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		// find if there is a rascal file associated to this configuration
		String path_mainModule = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, (String) null);

		// find if there is a rascal project associated to this configuration
		// (corresponds to a console launched from the contextual menu of a project)
		String path_project = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROJECT, (String) null);

		if(path_mainModule == null){
			ConsoleFactory consoleFactory = ConsoleFactory.getInstance();

			IRascalConsole console;

			if (mode.equals(ILaunchManager.RUN_MODE)) {

				if (path_project == null) {
					// open a Rascal Console
					console = consoleFactory.openRunConsole();
				} else {
					// open a Rascal Console associated to the given project
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					IProject project = root.getProject(path_project);
					if (project != null) {
						console = consoleFactory.openRunConsole(project);
					} 
				}
			} else if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				RascalDebugTarget target = new RascalDebugTarget(launch);
				if (path_project == null) {
					// open a Rascal Console
					console = ConsoleFactory.getInstance().openDebuggableConsole(target.getThread());
					target.setConsole(console);
				} else {
					IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
					IProject project = root.getProject(path_project);
					if (project != null) {
						// open a Rascal Console associated to the given project
						console = consoleFactory.openDebuggableConsole(project, target.getThread());
						target.setConsole(console);
					} 
				}
				launch.addDebugTarget(target);
			}else{
				throw new RuntimeException("Unknown mode: "+mode);
			}
		}else{
			ConsoleFactory consoleFactory = ConsoleFactory.getInstance();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.findMember(path_mainModule).getProject();
		
			IRascalConsole console;

			if (mode.equals(ILaunchManager.RUN_MODE)) {
				// open a Rascal Console
				console = consoleFactory.openRunOutputConsole(project);
			} else if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				//launch a debug session which opens automatically a new console
				RascalDebugTarget target = new RascalDebugTarget(launch);

				// open a Rascal Console
				console = ConsoleFactory.getInstance().openDebuggableOutputConsole(project, target.getThread());
				target.setConsole(console);

				launch.addDebugTarget(target);
			}else{
				throw new RuntimeException("Unknown mode: "+mode);
			}

			//construct the corresponding module name
			int index = path_mainModule.indexOf('/', 1);
			String moduleFullName = path_mainModule.substring(index+1);
			if(moduleFullName.startsWith("src/")) {
				moduleFullName = moduleFullName.replaceFirst("src/", "");		
			}
			moduleFullName = moduleFullName.replaceAll("/", "::");
			moduleFullName = moduleFullName.substring(0, moduleFullName.length()-Configuration.RASCAL_FILE_EXT.length());

			//import the module and launch the main function
			console.executeCommand("import "+moduleFullName+";");
			console.executeCommand("main();");
		}
	}
}
