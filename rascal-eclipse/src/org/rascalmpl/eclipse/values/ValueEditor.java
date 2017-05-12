package org.rascalmpl.eclipse.values;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.rascalmpl.eclipse.views.values.ValueEditorInput;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.uri.URIEditorInput;
import org.rascalmpl.uri.URIStorage;

import io.usethesource.vallang.ISourceLocation;

public class ValueEditor extends TextEditor  {
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        try {
            if (input instanceof URIEditorInput) {
                super.init(site, new ValueEditorInput((URIStorage) ((URIEditorInput) input).getStorage(), true, 2)); 
            }
            else {
                IResource tmp = input.getAdapter(IResource.class);
                
                if (tmp != null) {
                    ISourceLocation uri = ProjectURIResolver.constructProjectURI(tmp.getFullPath());
                    super.init(site, new ValueEditorInput(uri, true, 2));
                }
                
                throw new IOException("Value editor can not open " + input);
            }
        } catch (IOException | CoreException e) {
            throw new PartInitException("could not initialize editor", e);
        }
    }
}
