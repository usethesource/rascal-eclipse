package org.rascalmpl.eclipse.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorInput;
import org.rascalmpl.uri.URIEditorInput;

import io.usethesource.impulse.editor.EditorInputUtils;
import io.usethesource.impulse.services.IEditorInputResolver;

public class RascalEditorInputResolver implements IEditorInputResolver {

    public RascalEditorInputResolver() {
    }

    @Override
    public IPath getPath(IEditorInput editorInput) {
        if (editorInput instanceof URIEditorInput) {
            URIEditorInput input = (URIEditorInput) editorInput;
            return input.getStorage().getFullPath();
        }
        else {
            return EditorInputUtils.getPath(editorInput);
        }
    }

    @Override
    public IFile getFile(IEditorInput editorInput) {
        if (editorInput instanceof URIEditorInput) {
            return null;
        }
        else {
            return EditorInputUtils.getFile(editorInput);
        }
    }

    @Override
    public String getNameExtension(IEditorInput editorInput) {
        return getPath(editorInput).getFileExtension();
    }
}
