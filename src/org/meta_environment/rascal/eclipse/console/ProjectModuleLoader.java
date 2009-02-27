package org.meta_environment.rascal.eclipse.console;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.meta_environment.rascal.ast.Module;
import org.meta_environment.rascal.interpreter.errors.ModuleLoadException;
import org.meta_environment.rascal.interpreter.errors.SyntaxError;
import org.meta_environment.rascal.interpreter.load.LegacyModuleLoader;
import org.meta_environment.uptr.Factory;

public class ProjectModuleLoader extends LegacyModuleLoader {
	private static final String RASCAL_EXTENSION = "rsc";
	private static final String PACKAGE_SEPARATOR = "::";
	private static final String SRC_FOLDER_NAME = "src";

	@Override
	public Module loadModule(String name) throws ModuleLoadException {
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

			IFile file = root.getFileForLocation(getPath(path, name));

			if (file != null && file.exists()) {
				IConstructor tree;
				try {
					tree = PARSER.parseFromStream(file.getContents());
				} catch (FactTypeUseException e) {
					throw new ModuleLoadException(e.getMessage(), e);
				} catch (IOException e) {
					throw new ModuleLoadException(e.getMessage(), e);
				} catch (CoreException e) {
					throw new ModuleLoadException(e.getMessage(), e);
				}

				if (tree.getConstructorType() == Factory.ParseTree_Summary) {
					throw new SyntaxError(parseError(tree, name));
				}

				return BUILDER.buildModule(tree);		
			}
		}
		
		throw new ModuleLoadException("Module " + name + " not found");
	}

	private IPath getPath(IPath src, String moduleName) {
		String[] parts = moduleName.split(PACKAGE_SEPARATOR);
		IPath filePath = src;
		
		for (int i = 0; i < parts.length; i++) {
			filePath = filePath.append(parts[i]);
		}
		
		return  filePath.addFileExtension(RASCAL_EXTENSION);
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
}
