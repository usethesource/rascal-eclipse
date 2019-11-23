package org.rascalmpl.eclipse.views.xterm;

import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.ideservices.IDEServices;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.vallang.ISourceLocation;

public class XtermIDEServices implements IDEServices {

    @Override
    public void startJob(String name) {
        // TODO Auto-generated method stub
    }

    @Override
    public void startJob(String name, int totalWork) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void startJob(String name, int workShare, int totalWork) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void event(String name) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void event(String name, int inc) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void event(int inc) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int endJob(boolean succeeded) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isCanceled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void todo(int work) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void warning(String message, ISourceLocation src) {
        // TODO Auto-generated method stub
        
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

    

}