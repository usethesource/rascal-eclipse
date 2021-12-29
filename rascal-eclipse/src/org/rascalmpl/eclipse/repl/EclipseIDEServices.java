package org.rascalmpl.eclipse.repl;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.ideservices.IDEServices;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.impulse.runtime.RuntimePlugin;
import io.usethesource.vallang.ISourceLocation;

public class EclipseIDEServices implements IDEServices {


    @Override
    public void warning(String message, ISourceLocation src) {
        try (PrintStream consoleStream = RuntimePlugin.getInstance().getConsoleStream()) {
            consoleStream.println("[WARNING] " + src + ": " + message);
            consoleStream.flush();
        }
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
    public void jobStart(String name, int workShare, int totalWork) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void jobStep(String name, String message, int workShare) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int jobEnd(String name, boolean succeeded) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean jobIsCanceled(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void jobTodo(String name, int work) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public PrintWriter stderr() {
        // TODO Auto-generated method stub
        return null;
    }
}