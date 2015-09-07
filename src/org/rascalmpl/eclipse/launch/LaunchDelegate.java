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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.terminal.view.ui.interfaces.ILauncherDelegate;
import org.eclipse.tm.terminal.view.ui.launcher.LauncherDelegateManager;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.ConsoleFactory.IRascalConsole;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.IInterpreterEventListener;
import org.rascalmpl.interpreter.IInterpreterEventTrigger;
import org.rascalmpl.interpreter.InterpreterEvent;

public class LaunchDelegate implements ILaunchConfigurationDelegate{

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		LaunchConfigurationPropertyCache configurationUtility = new LaunchConfigurationPropertyCache(configuration);
		String moduleFullName = null;
		
		if(configurationUtility.hasPathOfMainModule()) {
            // FIXME: centralize URI schema <-> module / file name conversion.
            // construct the corresponding module name
            int index = configurationUtility.getPathOfMainModule().indexOf('/', 1);
            moduleFullName = configurationUtility.getPathOfMainModule().substring(index+1);
            
            if (moduleFullName.startsWith("src/org/rascalmpl/library/")) {
                moduleFullName = moduleFullName.replaceFirst("src/org/rascalmpl/library/", ""); 
            } 
            else if(moduleFullName.startsWith("src/")) {
                moduleFullName = moduleFullName.replaceFirst("src/", "");       
            } 
            else if(moduleFullName.startsWith("std/")) {
                moduleFullName = moduleFullName.replaceFirst("std/", "");
            }
            moduleFullName = moduleFullName.replaceAll("/", "::");
            moduleFullName = moduleFullName.substring(0, moduleFullName.length()-Configuration.RASCAL_FILE_EXT.length());
        }
		
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
	
	protected class RascalConsoleProcess implements IProcess, IInterpreterEventListener {
		
		private final ILaunch launch;
		
		private final IInterpreterEventTrigger eventTrigger;
		
		private final IRascalConsole console;
		
		public RascalConsoleProcess(ILaunch launch, IInterpreterEventTrigger eventTrigger, IRascalConsole console) {
			this.launch = launch;
			this.eventTrigger = eventTrigger;
			this.console = console;
			
			this.eventTrigger.addInterpreterEventListener(this);
		}
		
		@Override
		public <T> T getAdapter(Class<T> adapter) {
			return null;
		}

		@Override
		public boolean canTerminate() {
			return !console.isTerminated();
		}

		@Override
		public boolean isTerminated() {
			return console.isTerminated();
		}

		@Override
		public void terminate() throws DebugException {
			console.terminate();
		}

		@Override
		public String getLabel() {
			return "Rascal Console Process";
		}

		@Override
		public ILaunch getLaunch() {
			return launch;
		}

		@Override
		public IStreamsProxy getStreamsProxy() {
			return null;
		}

		@Override
		public void setAttribute(String key, String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getAttribute(String key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getExitValue() throws DebugException {
			return 0;
		}

		/**
		 * Fires a creation event.
		 */
		protected void fireCreationEvent() {
			fireEvent(new DebugEvent(this, DebugEvent.CREATE));
		}

		/**
		 * Fires the given debug event.
		 * 
		 * @param event debug event to fire
		 */
		protected void fireEvent(DebugEvent event) {
			DebugPlugin manager= DebugPlugin.getDefault();
			if (manager != null) {
				manager.fireDebugEventSet(new DebugEvent[]{event});
			}
		}

		/**
		 * Fires a terminate event.
		 */
		protected void fireTerminateEvent() {
			fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
		}		
		
		@Override
		public void handleInterpreterEvent(InterpreterEvent event) {
			switch (event.getKind()) {
			
			case CREATE:
				fireCreationEvent();
				break;

			case TERMINATE:
				fireTerminateEvent();
				this.eventTrigger.removeInterpreterEventListener(this);
				break;
				
			default:
				break;

			}
		}		
		
	}
	
}
