package org.meta_environment.rascal.eclipse.console;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.meta_environment.rascal.ast.Module;
import org.meta_environment.rascal.interpreter.errors.ModuleLoadException;
import org.meta_environment.rascal.interpreter.errors.SyntaxError;
import org.meta_environment.rascal.interpreter.load.LegacyModuleLoader;
import org.meta_environment.uptr.Factory;

public class ProjectModuleLoader extends LegacyModuleLoader {

	@Override
	public Module loadModule(String name) throws ModuleLoadException {
		String[] parts = name.split("::");
		
		if (parts.length > 1) {
			String projectName = parts[0];
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			
			if (project.exists()) {
				StringBuilder fileName = new StringBuilder();
				for (int i = 1; i < parts.length; i++) {
					fileName.append(parts[i]);
					if (i + 1 != parts.length) {
						fileName.append("/");
					}
				}
				fileName.append(RASCAL_FILE_EXT);
				IFile file = project.getFile(fileName.toString());
				
				if (file.exists()) {
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
		}
		
		throw new ModuleLoadException("Module " + name + " not found");
	}
}
