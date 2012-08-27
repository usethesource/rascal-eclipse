package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.jface.action.Action;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;

public abstract class AbstractProjectAction extends Action {
	protected final IProject project;

	public AbstractProjectAction(ISourceProject src, String label) {
		setText(label);
		setImageDescriptor(Activator.getInstance().getImageRegistry().getDescriptor(IRascalResources.RASCAL_DEFAULT_IMAGE));
		this.project = src != null ? src.getRawProject() : null;
	}
}
