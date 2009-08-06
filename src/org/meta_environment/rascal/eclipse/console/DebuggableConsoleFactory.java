package org.meta_environment.rascal.eclipse.console;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ui.console.IConsoleFactory;
import org.meta_environment.rascal.eclipse.IRascalResources;

public class DebuggableConsoleFactory implements IConsoleFactory {
	
	public DebuggableConsoleFactory() {
		super();
	}

	public void openConsole(){
		ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(IRascalResources.ID_RASCAL_LAUNCH_CONFIGURATION_TYPE);

		// create a new configuration which will launch the debuggable console
		ILaunchConfigurationWorkingCopy workingCopy;
		try {
			workingCopy = type.newInstance(null, "console debug");
			ILaunchConfiguration configuration = workingCopy.doSave();
			DebugUITools.launch(configuration, ILaunchManager.DEBUG_MODE);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

