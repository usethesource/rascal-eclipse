package org.rascalmpl.eclipse.repl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

public class JavaTerminalLauncher extends JavaLaunchDelegate implements ILaunchConfigurationDelegate {

    
    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        JavaTerminalConnector connector = (JavaTerminalConnector) configuration.getAttributes().get("connector");
        
        connector.getControl().setState(TerminalState.CONNECTED);
        
        // TODO: somehow connect a terminal here
        super.launch(configuration, mode, launch, monitor);
        
        
        launch.getProcesses()[0].getStreamsProxy();
    }
    

}
