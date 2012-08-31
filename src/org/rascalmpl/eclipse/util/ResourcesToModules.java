package org.rascalmpl.eclipse.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.rascalmpl.eclipse.IRascalResources;

public class ResourcesToModules {

	public static String moduleFromFile(IFile file) {
		IProject proj = file.getProject();
		if (proj != null && proj.exists()) {
			IFolder srcFolder = proj.getFolder(IRascalResources.RASCAL_SRC);
			if (srcFolder != null && srcFolder.exists()) {
				if (srcFolder.getProjectRelativePath().isPrefixOf(file.getProjectRelativePath())) {
					String name = file.getProjectRelativePath().removeFirstSegments(1).removeFileExtension().toPortableString();
					return name.replaceAll("/", "::");
				}
			}
		}

		return null;
	}
}
