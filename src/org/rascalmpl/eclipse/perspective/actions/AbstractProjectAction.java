package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.jface.action.Action;
import org.rascalmpl.eclipse.Activator;

public abstract class AbstractProjectAction extends Action {
	protected final IProject project;

	public AbstractProjectAction(ISourceProject src, String label) {
		super(label, Activator.getRascalImage());
		this.project = src != null ? src.getRawProject() : null;
	}
}
