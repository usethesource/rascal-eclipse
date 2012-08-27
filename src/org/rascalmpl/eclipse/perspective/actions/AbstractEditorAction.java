package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.jface.action.Action;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;

public abstract class AbstractEditorAction extends Action {
	protected IProject project;
	protected UniversalEditor editor;
	protected IFile file;

	public AbstractEditorAction(UniversalEditor editor, String label) {
		this.editor = editor;
		ISourceProject src = editor.getParseController().getProject();
		this.project = src != null ? src.getRawProject() : null;
		this.file = initFile(editor, project);
		setText(label);
		setImageDescriptor(Activator.getInstance().getImageRegistry().getDescriptor(IRascalResources.RASCAL_DEFAULT_IMAGE));
	}

	private IFile initFile(UniversalEditor editor, IProject project) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath path = editor.getParseController().getPath();
		
		if (project != null) {
			return project.getFile(path);
		}
		else {
			return workspaceRoot.getFile(path);
		}
	}
}
