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
import org.eclipse.imp.pdb.facts.IConstructor;
import org.meta_environment.rascal.interpreter.load.IModuleFileLoader;

public class ProjectModuleLoader implements IModuleFileLoader {
	private static final String SRC_FOLDER_NAME = "src";

	public IFile getFile(String name) throws IOException, CoreException {
		IWorkspaceRoot root = getWorkspaceRoot();

		for (IProject project : root.getProjects()) {
			if (project.isOpen()) {
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
		}

		throw new IOException("File " + name + " not found");	
	}

	public boolean fileExists(String filename){
		try{
			IFile file = getFile(filename);

			return (file != null && file.exists());
		}catch(Exception ex){
			return false;
		}
	}

	public InputStream getInputStream(String filename){
		try {
			IFile file = getFile(filename);

			if (file != null && file.exists()) {
				return file.getContents();
			} 
		}catch(Exception e){
			// Ignore, this is fine.
		}

		return null;
	}

	public boolean supportsLoadingBinaries(){
		return false;
	}

	public boolean tryWriteBinary(String filename, String binaryName, IConstructor tree){
		// Not implemented (yet).
		return false;
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
