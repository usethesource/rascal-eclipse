/*******************************************************************************
 * Copyright (c) 2009-2015 CWI
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.rascalmpl.eclipse.IRascalResources;

public class LaunchDelegate implements ILaunchConfigurationDelegate{

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		ILauncherDelegate delegate = LauncherDelegateManager.getInstance().getLauncherDelegate("org.rascalmpl.eclipse.rascal.launcher", false);

		if (delegate != null) {
		    Map<String, Object> properties = new HashMap<String, Object>();
		    properties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID, delegate.getId());
		    properties.put("project", launch.getLaunchConfiguration().getAttribute(IRascalResources.ATTR_RASCAL_PROJECT, (String) null));
		    properties.put("mode", launch.getLaunchMode());
		    properties.put("module", launch.getLaunchConfiguration().getAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, (String) null));
		    properties.put("launch", launch);
		    delegate.execute(properties, null);
		}
	}
}
