package org.rascalmpl.eclipse.nature;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.preferences.RascalPreferences;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;

import io.usethesource.impulse.builder.BuilderBase;
import io.usethesource.impulse.runtime.PluginBase;

public class Builder extends BuilderBase {

	@Override
	protected PluginBase getPlugin() {
		return Activator.getInstance();
	}

	@Override
	protected boolean isSourceFile(IFile file) {
		if (file == null || file.getFileExtension() == null) {
		  return false;
		}
		
		if (file.getFileExtension().equals(IRascalResources.RASCAL_EXT)) {
		  for (String root : new RascalEclipseManifest().getSourceRoots(file.getProject())) {
		    if (file.getProjectRelativePath().segment(0).equals(root)) {
		      return true;
		    }
		  }
		  
		  return file.getProjectRelativePath().segment(0).equals(IRascalResources.RASCAL_SRC);
		}
		
		return false;
	}

	@Override
	protected boolean isNonRootSourceFile(IFile file) {
		return false;
	}

	@Override
	protected boolean isOutputFolder(IResource resource) {
		return false;
	}

	@Override
	protected void compile(IFile file, IProgressMonitor monitor) { 
		if (RascalPreferences.isRascalCompilerEnabled()) {
			System.err.println("should run the static checker on " + file);
		}
	}

	@Override
	protected void collectDependencies(IFile file) { }

	@Override
	protected String getErrorMarkerID() {
		return IRascalResources.ID_RASCAL_MARKER;
	}

	@Override
	protected String getWarningMarkerID() {
		return IRascalResources.ID_RASCAL_MARKER;
	}

	@Override
	protected String getInfoMarkerID() {
		return IRascalResources.ID_RASCAL_MARKER;
	}
}
