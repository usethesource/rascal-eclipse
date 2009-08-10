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
package org.meta_environment.rascal.eclipse.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.meta_environment.rascal.eclipse.IRascalResources;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory.IRascalConsole;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalDebugTarget;
import org.meta_environment.rascal.interpreter.Configuration;
import org.meta_environment.rascal.interpreter.DebuggableEvaluator;

public class LaunchDelegate implements ILaunchConfigurationDelegate{

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		ConsoleFactory consoleFactory = ConsoleFactory.getInstance();
		
		IRascalConsole console;
		
		if (mode.equals(ILaunchManager.RUN_MODE)) {
			// open a Rascal Console
			console = consoleFactory.openRunConsole();
		} else if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			//launch a debug session which opens automatically a new console
			RascalDebugTarget target = new RascalDebugTarget(launch);
			
			// open a Rascal Console
			console = ConsoleFactory.getInstance().openDebuggableConsole(target.getThread());
			target.setConsole(console);
			
			//activate the step by step statement mode by default
			((DebuggableEvaluator) console.getRascalInterpreter().getEval()).setStatementStepMode(true);
			launch.addDebugTarget(target);
		}else{
			throw new RuntimeException("Unknown mode: "+mode);
		}

		// find if there is a rascal file associated to this configuration
		String path = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, (String) null);
		// TODO should find a way to specify the src folders for a Rascal project
		// see also ProjectModuleLoader
		
		if(path != null){
			//construct the corresponding module name
			int index = path.indexOf('/', 1);
			String moduleFullName = path.substring(index+1);
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
