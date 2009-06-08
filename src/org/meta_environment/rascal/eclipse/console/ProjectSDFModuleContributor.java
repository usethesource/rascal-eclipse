package org.meta_environment.rascal.eclipse.console;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.meta_environment.rascal.interpreter.load.ISdfSearchPathContributor;

public class ProjectSDFModuleContributor implements ISdfSearchPathContributor{
	private static final String SRC_FOLDER_NAME = "src";

	public List<String> contributePaths(){
		List<String> sdfSearchPath = new ArrayList<String>();
		
		IWorkspaceRoot root = getWorkspaceRoot();
		
		IProject[] projects = root.getProjects();
		
		for(int i = projects.length - 1; i >= 0; i--){
			IProject project = projects[i];
			
			IPath path;
			IFolder srcFolder = project.getFolder(SRC_FOLDER_NAME);
			if(srcFolder.exists()){
				path = srcFolder.getLocation();
			}else{
				path = project.getLocation();
			}
			
			sdfSearchPath.add(path.toOSString());
		}
		
		return sdfSearchPath;
	}

	private static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
