package org.meta_environment.rascal.eclipse.console;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.meta_environment.rascal.interpreter.load.IModuleFileLoader;

public class ProjectModuleLoader implements IModuleFileLoader {
	private static final String SRC_FOLDER_NAME = "src";

	public IFile getFile(String name) throws IOException, CoreException {
		IWorkspaceRoot root = getWorkspaceRoot();
		
		for (IProject project : root.getProjects()) {
			IFolder srcFolder = project.getFolder(SRC_FOLDER_NAME);
			IPath path;
			
			if (srcFolder.exists()) {
				path = srcFolder.getLocation();
			}
			else {
				path = project.getLocation();
			}

			IFile file = root.getFileForLocation(path.append(name));
			
			if (file.exists()) {
				file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
				return file;
			}
		}
		
		throw new IOException("File " + name + " not found");	
	}
	
	public InputStream getInputStream(String name) throws IOException {
		try {
			IFile file = getFile(name);

			if (file != null && file.exists()) {
				return file.getContents();
			} 
		}
		catch (CoreException e) {
			throw new IOException(e.getMessage());
		}
		
		throw new IOException("File " + name + " not found");	
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
