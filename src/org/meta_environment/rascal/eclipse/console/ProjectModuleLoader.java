package org.meta_environment.rascal.eclipse.console;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.meta_environment.rascal.interpreter.load.AbstractModuleLoader;

public class ProjectModuleLoader extends AbstractModuleLoader {
	private static final String SRC_FOLDER_NAME = "src";

	public IFile getFile(String name) throws IOException {
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
				return file;
			}
		}
		
		throw new IOException("File " + name + " not found");	
	}
	
	@Override
	protected InputStream getStream(String name) throws IOException {
		IFile file = getFile(name);
		
		if (file != null && file.exists()) {
			try {
				return file.getContents();
			} catch (CoreException e) {
				throw new IOException(e.getMessage(), e);
			}
		}
		
		throw new IOException("File " + name + " not found");	
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
