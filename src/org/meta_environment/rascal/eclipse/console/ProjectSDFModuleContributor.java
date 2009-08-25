package org.meta_environment.rascal.eclipse.console;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.meta_environment.rascal.eclipse.IRascalResources;
import org.meta_environment.rascal.interpreter.load.ISdfSearchPathContributor;

public class ProjectSDFModuleContributor implements ISdfSearchPathContributor{

	private final IProject project;

	public ProjectSDFModuleContributor(IProject project) {
		this.project = project;
	}

	public List<String> contributePaths() {
		List<String> sdfSearchPath = new ArrayList<String>();
		LinkedList<IProject> projects = new LinkedList<IProject>();
		projects.add(project);

		while (! projects.isEmpty()) {
			IProject project = projects.removeFirst();
			if (project.isOpen()) {
				IFolder srcFolder = project.getFolder(IRascalResources.RASCAL_SRC);
				IPath path;
				if (srcFolder.exists()) {
					path = srcFolder.getLocation();
					sdfSearchPath.add(path.toOSString());
				}
				try {
					projects.addAll(Arrays.asList(project.getDescription().getReferencedProjects()));
				} catch (CoreException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return sdfSearchPath;
	}

}
