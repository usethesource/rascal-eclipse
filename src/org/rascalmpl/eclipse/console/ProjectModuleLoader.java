package org.rascalmpl.eclipse.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.LinkedList;

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
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.interpreter.load.IModuleFileLoader;

public class ProjectModuleLoader implements IModuleFileLoader {

	private final IProject project;
	
	public ProjectModuleLoader(IProject project) {
		this.project = project;
	}

	public IFile getFile(String name) throws IOException, CoreException {
		IWorkspaceRoot root = getWorkspaceRoot();
		LinkedList<IProject> projects = new LinkedList<IProject>();
		projects.add(project);

		while (! projects.isEmpty()) {
			IProject project = projects.removeFirst();
			if (project.isOpen()) {
				IFolder srcFolder = project.getFolder(IRascalResources.RASCAL_SRC);
				IPath path;
				if (srcFolder.exists()) {
					path = srcFolder.getLocation();
					IFile file = root.getFileForLocation(path.append(name));

					if (file.exists()) {
						file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
						return file;
					}
				}
				projects.addAll(Arrays.asList(project.getDescription().getReferencedProjects()));

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

	public URI getURI(String filename) {
		try {
			return URI.create("project://" + project.getName() + URLEncoder.encode(filename, "UTF8"));
		} catch (UnsupportedEncodingException e) {
			// TODO don't know what to do yet here
			return null;
		}
	}
}
