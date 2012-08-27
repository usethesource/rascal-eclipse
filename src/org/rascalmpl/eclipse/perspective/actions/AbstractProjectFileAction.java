package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.imp.model.ISourceProject;

public abstract class AbstractProjectFileAction extends AbstractProjectAction {
	protected final IFile file;

	public AbstractProjectFileAction(ISourceProject src, IFile file, String label) {
		super(src, label);
		this.file = file;
	}
}
