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
    public void startJob(String name) {
        monitor.beginTask(name, -1);
    }

    @Override
    public void startJob(String name, int totalWork) {
        monitor.beginTask(name, totalWork);
    }

    @Override
    public void startJob(String name, int workShare, int totalWork) {
        monitor.beginTask(name, totalWork);
    }

    @Override
    public void event(String name) {
        monitor.subTask(name);
    }

    @Override
    public void event(String name, int inc) {
        monitor.subTask(name);
        monitor.worked(inc);
    }

    @Override
    public void event(int inc) {
        monitor.worked(inc);
    }

    @Override
    public int endJob(boolean succeeded) {
        monitor.done();
        return -1;
    }

    @Override
    public boolean isCanceled() {
        return monitor.isCanceled();
    }

    @Override
    public void todo(int work) {
        
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