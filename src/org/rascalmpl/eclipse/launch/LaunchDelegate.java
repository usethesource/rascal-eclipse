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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.rascalmpl.eclipse.console.ConsoleFactory;
import org.rascalmpl.eclipse.console.ConsoleFactory.IRascalConsole;
import org.rascalmpl.eclipse.debug.core.model.RascalDebugTarget;
import org.rascalmpl.interpreter.Configuration;

public class LaunchDelegate implements ILaunchConfigurationDelegate{

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		LaunchConfigurationPropertyCache configurationUtility = new LaunchConfigurationPropertyCache(configuration);
		
		/*
		 * Launching console in either run or debug configuration
		 */
		ConsoleFactory consoleFactory = ConsoleFactory.getInstance();
		IRascalConsole console = null;
		
		if (mode.equals(ILaunchManager.RUN_MODE)) {
	
			if (configurationUtility.hasAssociatedProject()) {
				console = consoleFactory.openRunConsole(configurationUtility.getAssociatedProject());
			} else {
				console = consoleFactory.openRunConsole();				
			}
			
		} else if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			
			// create a new debug session
			RascalDebugTarget debugTarget = new RascalDebugTarget(launch);

			if (configurationUtility.hasAssociatedProject()) {
				console = consoleFactory.openDebuggableConsole(configurationUtility.getAssociatedProject(), debugTarget.getThread());
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
		 * If a main module is present, import it and launch its main() function.
		 */
		if(configurationUtility.hasPathOfMainModule()) {
			
			// FIXME: centralize URI schema <-> module / file name conversion.
			// construct the corresponding module name
			int index = configurationUtility.getPathOfMainModule().indexOf('/', 1);
			String moduleFullName = configurationUtility.getPathOfMainModule().substring(index+1);
			if(moduleFullName.startsWith("src/")) {
				moduleFullName = moduleFullName.replaceFirst("src/", "");		
			} else if(moduleFullName.startsWith("std/")) {
				moduleFullName = moduleFullName.replaceFirst("std/", "");
			}
			moduleFullName = moduleFullName.replaceAll("/", "::");
			moduleFullName = moduleFullName.substring(0, moduleFullName.length()-Configuration.RASCAL_FILE_EXT.length());

			console.activate();
			console.executeCommand("import " + moduleFullName + ";");
			console.executeCommand("main();");
			
		}
			
	}
	
}
