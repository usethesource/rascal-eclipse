package org.rascalmpl.eclipse.perspective.actions.highlight;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.editor.UniversalEditor;

class StringStorage implements IStorage {
	private String string;
	private UniversalEditor editor;
	private IProject project;
	private String ext;

	StringStorage(UniversalEditor editor, IProject project, String input, String ext) {
		this.editor = editor;
		this.project = project;
		this.string = input;
		this.ext = ext;
	}

	public InputStream getContents() throws CoreException {
		return new ByteArrayInputStream(string.getBytes());
	}

	public IPath getFullPath() {
		return project.getFullPath().addFileExtension(ext);
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public String getName() {
		return editor.getEditorInput().getName() + "." + ext;
	}

	public boolean isReadOnly() {
		return true;
	}
}

