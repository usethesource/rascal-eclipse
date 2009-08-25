package org.meta_environment.rascal.eclipse.debug.core.sourcelookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.meta_environment.rascal.eclipse.IRascalResources;

public class RascalSourcePathComputerDelegate implements ISourcePathComputerDelegate{

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {

		String mainModule = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROGRAM, (String)null);
		String mainProject = configuration.getAttribute(IRascalResources.ATTR_RASCAL_PROJECT, (String)null);

		LinkedList<ISourceContainer> sourceContainers = new  LinkedList<ISourceContainer>();

		if (mainProject != null) {
			//when a project is specified, lookup in the project and its referenced projects
			LinkedList<IProject> projects = new LinkedList<IProject>();
			projects.add(ResourcesPlugin.getWorkspace().getRoot().getProject(mainProject));

			while (! projects.isEmpty()) {
				IProject project = projects.removeFirst();
				sourceContainers.add(new ProjectSourceContainer(project, false));
				projects.addAll(Arrays.asList(project.getDescription().getReferencedProjects()));
			}
			return sourceContainers.toArray(new ISourceContainer[]{});
		}	

		if (mainModule != null) {
			//when a specific file is specified, lookup in its corresponding project (+ referenced projects)
			//when a project is specified, lookup in the project and its referenced projects
			LinkedList<IProject> projects = new LinkedList<IProject>();
			projects.add(ResourcesPlugin.getWorkspace().getRoot().findMember(mainModule).getProject());

			while (! projects.isEmpty()) {
				IProject project = projects.removeFirst();
				sourceContainers.add(new ProjectSourceContainer(project, false));
				projects.addAll(Arrays.asList(project.getDescription().getReferencedProjects()));
			}
			return sourceContainers.toArray(new ISourceContainer[]{});
			}
		/* default case */
		return new ISourceContainer[]{};
	}
}
