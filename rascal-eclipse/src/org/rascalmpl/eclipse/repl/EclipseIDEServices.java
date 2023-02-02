package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.eclipse.editor.MessagesToMarkers;
import org.rascalmpl.eclipse.nature.IWarningHandler;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.ideservices.IDEServices;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.values.IRascalValueFactory;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.impulse.builder.MarkerCreator;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISetWriter;
import io.usethesource.vallang.ISourceLocation;

public class EclipseIDEServices extends RascalMonitor implements IDEServices {

    public EclipseIDEServices(IProgressMonitor monitor, IWarningHandler handler) {
        super(monitor, handler);
    }


    @Override
    public void registerDiagnostics(IList messages) {
        Map<ISourceLocation, ISetWriter> grouped = groupErrorsBySourceFile(messages);
        
        grouped.keySet().stream().forEach(loc -> {
            if (!loc.getScheme().equals("project")) {
                // don't know how to handle those
                return;
            }
            
            try {
                new MessagesToMarkers().process(loc, grouped.get(loc).done(), 
                        new MarkerCreator(new ProjectURIResolver().resolveFile(loc), IRascalResources.ID_RASCAL_MARKER));
            } catch (IOException e) {
                // ignore messages for files that have dissappeared
            }
        });
    }
    
    @Override
    public void unregisterDiagnostics(IList resources) { 
       resources.stream().forEach(loc -> {
            try {
                IResource file = new ProjectURIResolver().resolveFile((ISourceLocation) loc);
                file.deleteMarkers(IRascalResources.ID_RASCAL_MARKER, true, IFile.DEPTH_ZERO);
            } catch (IOException | CoreException e) {
                // ignore file errors. can't remove markers from files that don't exist anymore
            }
       });
    }
    
    private Map<ISourceLocation, ISetWriter> groupErrorsBySourceFile(IList messages) {
        Map<ISourceLocation, ISetWriter> grouped = new HashMap<>();
        
        // group the list of messages per source file
        messages.stream().forEach(m -> {
            ISourceLocation loc = ((ISourceLocation) ((IConstructor) m).get(1)).top();
            ISetWriter forFile = grouped.get(loc);
            if (forFile == null) {
                forFile = IRascalValueFactory.getInstance().setWriter();
                grouped.put(loc, forFile);
            }
            forFile.append(m);
        });
        
        return grouped;
    }
    
    @Override
    public void browse(URI uri) {
        new UIJob("start browser for " + uri) {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                EditorUtil.openWebURI(ValueFactoryFactory.getValueFactory().sourceLocation(uri));
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    @Override
    public void edit(ISourceLocation path) {
        new UIJob("start editor for " + path) {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                EditorUtil.openAndSelectURI(path);
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    @Override
    public PrintWriter stderr() {
        assert false;
        return new PrintWriter(System.err);
    }
}