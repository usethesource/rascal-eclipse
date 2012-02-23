package org.rascalmpl.eclipse.nature;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.builder.BuilderBase;
import org.eclipse.imp.runtime.PluginBase;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.preferences.RascalPreferences;

public class Builder extends BuilderBase {

	@Override
	protected PluginBase getPlugin() {
		return Activator.getInstance();
	}

	@Override
	protected boolean isSourceFile(IFile file) {
		if (file.getFileExtension().equals(IRascalResources.RASCAL_EXT)) {
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
		if (RascalPreferences.isStaticCheckerEnabled()) {
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
