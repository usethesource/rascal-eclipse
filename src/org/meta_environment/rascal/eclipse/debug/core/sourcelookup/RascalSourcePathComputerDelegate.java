package org.meta_environment.rascal.eclipse.debug.core.sourcelookup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.meta_environment.rascal.eclipse.IRascalResources;

public class RascalSourcePathComputerDelegate implements ISourcePathComputerDelegate{

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {

		List<ISourceContainer> containers = new ArrayList<ISourceContainer>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		for (IProject project : root.getProjects()) {
			if (project.isOpen()) {
				containers.add(new ProjectSourceContainer(project, false));
			}
		}
		return containers.toArray( new ISourceContainer[]{});

		/**
		 //this code will be used when debugging a specific file
		String path = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, (String)null);
		ISourceContainer sourceContainer = null;
		if (path != null) {
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
				IProject project = resource.getProject();
				sourceContainer = new ProjectSourceContainer(project, false);
				return new ISourceContainer[]{sourceContainer};
		}
	}
		 */

	}
}
