package org.rascalmpl.eclipse.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.model.ISourceProject;

public abstract class AbstractEditorAction extends AbstractProjectFileAction {
	protected UniversalEditor editor;

	public AbstractEditorAction(UniversalEditor editor, String label) {
		super(editor != null ? editor.getParseController().getProject() : null, initFile(editor, editor != null ? editor.getParseController().getProject() : null), label);
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
