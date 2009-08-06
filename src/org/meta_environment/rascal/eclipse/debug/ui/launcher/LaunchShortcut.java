package org.meta_environment.rascal.eclipse.debug.ui.launcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.meta_environment.rascal.eclipse.IRascalResources;

/**
 * Launches a RASCAL file
 */
public class LaunchShortcut implements ILaunchShortcut {

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.jface.viewers.ISelection, java.lang.String)
     */
    public void launch(ISelection selection, String mode) {
        // must be a structured selection with one file selected
        IFile file = (IFile) ((IStructuredSelection)selection).getFirstElement();

        // check for an existing launch config for the rascal file
        String path = file.getFullPath().toString(); 
        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(IRascalResources.ID_RASCAL_LAUNCH_CONFIGURATION_TYPE);
        try {
            ILaunchConfiguration[] configurations = launchManager.getLaunchConfigurations(type);
            for (int i = 0; i < configurations.length; i++) {
                ILaunchConfiguration configuration = configurations[i];
                String attribute = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, (String)null);
                if (path.equals(attribute)) {
                    DebugUITools.launch(configuration, mode);
                    return;
                }
            }
        } catch (CoreException e) {
            return;
        }
        
        try {
            // create a new configuration for the rascal file
            ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, file.getName());
            workingCopy.setAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, path);
            ILaunchConfiguration configuration = workingCopy.doSave();
            DebugUITools.launch(configuration, mode);
        } catch (CoreException e1) {
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.ui.IEditorPart, java.lang.String)
     */
    public void launch(IEditorPart editor, String mode) {
    }

}
