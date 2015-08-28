package org.rascalmpl.eclipse.repl;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

public class JavaTerminalLauncher extends JavaLaunchDelegate implements ILaunchConfigurationDelegate {

    
    @Override
    public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
            throws CoreException {
        launch.getProcesses()[0].getStreamsProxy();
        // TODO: somehow connect a terminal here
        super.launch(configuration, mode, launch, monitor);
    }
    
    @Override
    public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
            throws CoreException {
        // TODO Auto-generated method stub
        return super.preLaunchCheck(configuration, mode, monitor);
    }

}
