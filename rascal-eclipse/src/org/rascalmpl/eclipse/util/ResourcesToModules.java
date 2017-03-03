package org.rascalmpl.eclipse.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.rascalmpl.interpreter.load.RascalSearchPath;
import org.rascalmpl.uri.ProjectURIResolver;
import io.usethesource.vallang.ISourceLocation;

public class ResourcesToModules {

	public static String moduleFromFile(IFile file) {
		IProject proj = file.getProject();
		if (proj != null && proj.exists()) {
		  for (String root : new RascalEclipseManifest().getSourceRoots(proj)) {
		    String mod = moduleForRoot(file, proj.getFolder(root));
		    if (mod != null) {
		    	return mod;
		    }
		  }
		}

		return null;
	}

	private static String moduleForRoot(IFile file, IFolder stdFolder) {
		if (stdFolder != null && stdFolder.exists()) {
			if (stdFolder.getProjectRelativePath().isPrefixOf(file.getProjectRelativePath())) {
			    int segments = stdFolder.getProjectRelativePath().segmentCount();
				String name = file.getProjectRelativePath().removeFirstSegments(segments).removeFileExtension().toPortableString();
				name = name.replaceAll("/", "::");
				return name.length() == 0 ? null : name;
			}
		}
		
		return null;
	}
	
	public static URI uriFromModule(RascalSearchPath resolver, String module) {
		ISourceLocation uri = resolver.resolveModule(module);

		if (uri.getScheme().equals("project")) {
		  try {
		    return new ProjectURIResolver().resolveFile(uri).getLocationURI();
		  } 
		  catch (MalformedURLException e) {
		    // nothing
		  } 
		  catch (IOException e) {
		    // nothing
		  }
		}

		return uri.getURI();
	}
}
