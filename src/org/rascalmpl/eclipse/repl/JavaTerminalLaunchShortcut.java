package org.rascalmpl.eclipse.repl;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.internal.launching.JavaAppletLaunchConfigurationDelegate;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.rascalmpl.eclipse.Activator;

public class JavaTerminalLaunchShortcut implements ILaunchShortcut {

    @Override
    public void launch(ISelection selection, String mode) {
        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type = launchManager.getLaunchConfigurationType("rascal-eclipse.java.terminal");
        IFile file = (IFile) ((IStructuredSelection)selection).getFirstElement();
        String resourceFullPath = file.getFullPath().toString();
        
//        try {
//            ILaunchConfiguration[] configurations = launchManager.getLaunchConfigurations(type);
//            for (int i = 0; i < configurations.length; i++) {
//                ILaunchConfiguration configuration = configurations[i];
//                String attribute = configuration.getAttribute("class", (String)null);
//                if (resourceFullPath.equals(attribute)) {
//                    DebugUITools.launch(configuration, mode);
//                    return;
//                }
//            }
//        } catch (CoreException e) {
//            IStatus message = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
//            Activator.getInstance().getLog().log(message);
//            return;
//        }
//        
        try {
            // create a new configuration for the rascal file
            ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, file.getName());
            workingCopy.setAttribute("class", resourceFullPath);
            ILaunchConfiguration configuration = workingCopy.doSave();
            DebugUITools.launch(configuration, mode);
        } catch (CoreException e1) {
            IStatus message = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e1.getMessage(), e1);
            Activator.getInstance().getLog().log(message);
        }
    }

    @Override
    public void launch(IEditorPart editor, String mode) {
        Activator.log("launch not implemented for editors yet", new IllegalArgumentException());
    }

}
