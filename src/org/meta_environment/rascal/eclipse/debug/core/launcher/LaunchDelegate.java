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
package org.meta_environment.rascal.eclipse.debug.core.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory.RascalConsole;
import org.meta_environment.rascal.eclipse.debug.core.model.RascalDebugTarget;


public class LaunchDelegate implements ILaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
		    RascalDebugTarget target = new RascalDebugTarget(launch);
			//activate the step by step statement mode by default
			target.getEvaluator().setStatementStepMode(true);
			launch.addDebugTarget(target);
		}
	}


}
