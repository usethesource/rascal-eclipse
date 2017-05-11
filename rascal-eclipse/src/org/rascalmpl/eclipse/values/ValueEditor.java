package org.rascalmpl.eclipse.values;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.views.values.ValueEditorInput;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.exceptions.FactTypeUseException;
import io.usethesource.vallang.io.StandardTextReader;
import io.usethesource.vallang.io.binary.stream.IValueOutputStream;

public class ValueEditor extends TextEditor  {
    private ISourceLocation uri = null;

    @Override
    public void doSave(IProgressMonitor progressMonitor) {
        try {
            if (isDirty()) {
                String content = getDocumentProvider().getDocument(getEditorInput()).get();
                IValue val = new StandardTextReader().read(ValueFactoryFactory.getValueFactory(), new StringReader(content));
                IValueOutputStream out = new IValueOutputStream(URIResolverRegistry.getInstance().getOutputStream(uri, false), ValueFactoryFactory.getValueFactory());
                out.write(val);
                out.close();
            }
        } catch (FactTypeUseException | IOException e) {
            Activator.log("could not save this file", e);
        }
    }
    
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        try {
            this.uri = ProjectURIResolver.constructProjectURI(input.getAdapter(IResource.class).getFullPath());
            super.init(site, new ValueEditorInput(uri, true, 2));
        } catch (IOException e) {
            throw new PartInitException("could not initialize editor", e);
        }
    }
}
