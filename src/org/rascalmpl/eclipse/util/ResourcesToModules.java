package org.rascalmpl.eclipse.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.interpreter.load.RascalURIResolver;

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
	
	public static URI uriFromModule(RascalURIResolver resolver, String module) {
		URI uri = resolver.resolve(URI.create("rascal://" + module));

		if (uri.getScheme().equals("std")) {
			return URI.create("rascal-library://rascal" + uri.getPath());
		}
		else if (uri.getScheme().equals("eclipse")) {
			return URI.create("rascal-library://eclipse" + uri.getPath());
		}
		else if (uri.getScheme().equals("project")) {
			try {
				return new ProjectURIResolver().resolveFile(uri).getLocationURI();
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}
		}

		return uri;
	}
}
