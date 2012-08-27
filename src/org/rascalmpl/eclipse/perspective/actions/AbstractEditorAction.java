package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.model.ISourceProject;

public abstract class AbstractEditorAction extends AbstractProjectFileAction {
	protected final UniversalEditor editor;

	public AbstractEditorAction(UniversalEditor editor, String label) {
		super(editor.getParseController().getProject(), initFile(editor, editor.getParseController().getProject()), label);
		this.editor = editor;
	}

	private static IFile initFile(UniversalEditor editor, ISourceProject project) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath path = editor.getParseController().getPath();
		
		if (project != null) {
			return project.getRawProject().getFile(path);
		}
		else {
			return workspaceRoot.getFile(path);
		}
	}
}
