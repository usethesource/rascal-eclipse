package org.meta_environment.rascal.eclipse.nature;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.meta_environment.rascal.eclipse.IRascalResources;

public class Nature implements IProjectNature {
	private IProject project;

	public void configure() throws CoreException {
		IFolder folder = project.getFolder(IRascalResources.RASCAL_SRC);
		folder.create(false, false, null);
	}

	public void deconfigure() throws CoreException {

	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;

	}

}
