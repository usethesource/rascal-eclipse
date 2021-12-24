package org.rascalmpl.eclipse.util;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.rascalmpl.debug.IRascalMonitor;
import org.rascalmpl.eclipse.Activator;

import io.usethesource.vallang.ISourceLocation;

public class RascalProgressMonitor implements IRascalMonitor {
    private final IProgressMonitor monitor;

    public RascalProgressMonitor(IProgressMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void jobStart(String name, int workShare, int totalWork) {
        monitor.beginTask(name, totalWork);
    }

    @Override
    public boolean jobIsCanceled(String name) {
        return monitor.isCanceled();
    }
    
    @Override
    public void jobStep(String name, String message, int workShare) {
        monitor.worked(workShare);
    }

    @Override
    public int jobEnd(String name, boolean succeeded) {
        monitor.done();
        return 0;
    }

    @Override
    public void jobTodo(String name, int work) {

    }
    

    @Override
    public void warning(String message, ISourceLocation src) {
        try {
            ThreadSafeImpulseConsole.INSTANCE.getWriter().write(src + ":" + message + "\n");
        } catch (IOException e) {
            Activator.log("failed to print warning", e);
        }
    }

   
}