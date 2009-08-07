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
import org.meta_environment.rascal.eclipse.console.internal.CommandExecutionException;
import org.meta_environment.rascal.eclipse.console.internal.TerminationException;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalDebugTarget;
import org.meta_environment.rascal.interpreter.Configuration;


public class LaunchDelegate implements ILaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (mode.equals(ILaunchManager.RUN_MODE)) {

			// open a Rascal Console
			ConsoleFactory.getInstance().openConsole();

		} else if (mode.equals(ILaunchManager.DEBUG_MODE)) {

			//launch a debug session which opens automatically a new console
			RascalDebugTarget target = new RascalDebugTarget(launch);
			//activate the step by step statement mode by default
			target.getEvaluator().setStatementStepMode(true);
			launch.addDebugTarget(target);

		}

		// find if there is a rascal file associated to this configuration
		String path = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, (String)null);
		// TODO should find a way to specify the src folders for a Rascal project
		// see also ProjectModuleLoader

		if (path != null) {

			//construct the corresponding module name
			int index = path.indexOf('/', 1);
			String moduleFullName = path.substring(index+1);
			if (moduleFullName.startsWith("src")) {
				moduleFullName.replaceFirst("src/", "");		
			}
			moduleFullName = moduleFullName.replaceAll("/", "::");
			moduleFullName = moduleFullName.substring(0, moduleFullName.length()-Configuration.RASCAL_FILE_EXT.length());

			//import the module and launch the main function
			try {
				ConsoleFactory factory = ConsoleFactory.getInstance();
				factory.getLastConsole().getRascalInterpreter().execute("import "+moduleFullName+";");
				factory.getLastConsole().getRascalInterpreter().execute("main();");
			} catch (CommandExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TerminationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
