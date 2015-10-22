package org.rascalmpl.eclipse.editor.commands;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;

import io.usethesource.impulse.editor.UniversalEditor;
import io.usethesource.impulse.model.ISourceProject;

public abstract class AbstractEditorAction extends Action {
	protected final UniversalEditor editor;
	protected IProject project;
	protected IFile file;

	public AbstractEditorAction(UniversalEditor editor, String label) {
	    super(label);
		this.project = editor != null && editor.getParseController().getProject() != null ? editor.getParseController().getProject().getRawProject() : null;
		this.file = initFile(editor, editor != null ? editor.getParseController().getProject() : null);
		this.editor = editor;
	}

	protected static IFile initFile(UniversalEditor editor, ISourceProject project) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		if (editor == null) {
			return null;
		}
		
		IPath path = editor.getParseController().getPath();
		
		if (project != null) {
			return project.getRawProject().getFile(path);
		}
		else {
			return workspaceRoot.getFile(path);
		}
	}
}
