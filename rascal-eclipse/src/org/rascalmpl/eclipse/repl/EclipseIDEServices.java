package org.rascalmpl.eclipse.repl;

import java.io.PrintWriter;
import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.eclipse.library.util.ResourceMarkers;
import org.rascalmpl.eclipse.nature.IWarningHandler;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.util.ThreadSafeImpulseConsole;
import org.rascalmpl.ideservices.IDEServices;
import org.rascalmpl.values.IRascalValueFactory;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISourceLocation;

public class EclipseIDEServices extends RascalMonitor implements IDEServices {
    private static final PrintWriter CONSOLE_WRITER = new PrintWriter(ThreadSafeImpulseConsole.INSTANCE.getWriter());
    ResourceMarkers markers = new ResourceMarkers();

    public EclipseIDEServices(IProgressMonitor monitor, IWarningHandler handler) {
        super(monitor, handler);
    }


    @Override
    public void registerDiagnostics(IList messages) {
        markers.addMessageMarkers(messages.stream().collect(IRascalValueFactory.getInstance().setWriter()));
    }
    
    @Override
    public void unregisterDiagnostics(IList resources) { 
       resources.stream().forEach(loc -> {
            markers.removeMessageMarkers((ISourceLocation) loc);
       });
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
        return CONSOLE_WRITER;
    }
}