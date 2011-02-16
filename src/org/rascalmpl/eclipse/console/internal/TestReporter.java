package org.rascalmpl.eclipse.console.internal;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.interpreter.ITestResultListener;

public class TestReporter implements ITestResultListener {
//	private final IProgressMonitor monitor;
	private static HashSet<IMarker> markers = new HashSet<IMarker>();

	public TestReporter() {
//		this.monitor = monitor;
	}

	public void done() {
//		monitor.done();
	}

	public void report(boolean successful, String test, ISourceLocation loc) {
		report(successful, test, loc, null);
	}
	
	public void report(final boolean successful, String test, final ISourceLocation loc, final Throwable t) {
//		monitor.worked(1);
		final IFile file = getFile(loc);
		
		try {
			IMarker m = file.createMarker("rascal.markerType.testResult");
			Map<String,Object> attrs = new HashMap<String,Object>();
			attrs.put(IMarker.TRANSIENT, true);
			attrs.put(IMarker.CHAR_START, loc.getOffset());
			attrs.put(IMarker.CHAR_END, loc.getOffset() + loc.getLength());
			attrs.put(IMarker.MESSAGE, t != null ? t.getMessage() : (successful ? "succeeded" : "failed"));
			attrs.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
			attrs.put(IMarker.SEVERITY, successful ? IMarker.SEVERITY_INFO : IMarker.SEVERITY_ERROR);
			m.setAttributes(attrs);
			markers.add(m);
		} catch (CoreException e) {
			Activator.getInstance().logException(e.getMessage(), e);
		}
		
	}

	public void start(int count) {
		
//		try {
//			ResourcesPlugin.getWorkspace().getRoot().deleteMarkers("rascal.markerType.testResult", false, IResource.DEPTH_INFINITE);
//			file.deleteMarkers("rascal.markerType.testResult", false, IResource.DEPTH_INFINITE);
//			monitor.beginTask("Rascal Test Runner", count);
//			for (IMarker m : markers) m.delete();
//		} catch (CoreException e) {
//			Activator.getInstance().logException(e.getMessage(), e);
//		}
	}
	
	private IFile getFile(ISourceLocation loc) {
		URI uri = loc.getURI();
		String scheme = uri.getScheme();
		
		if (scheme.equals("project")) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getHost());
			
			if (project != null) {
				return project.getFile(uri.getPath());
			}
			
			Activator.getInstance().logException("project " + uri.getHost() + " does not exist", new RuntimeException());
		}
		else if (scheme.equals("file")) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IFile[] cs = root.findFilesForLocationURI(uri);
			
			if (cs != null && cs.length > 0) {
				return cs[0];
			}
			
			Activator.getInstance().logException("file " + uri + " not found", new RuntimeException());
		}
		else if (scheme.equals("rascal-library")) {
			IFile [] files =ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
			if (files.length > 0) {
				return files[0];
			}
		}
		
		Activator.getInstance().logException("scheme " + uri.getScheme() + " not supported", new RuntimeException());
		return null;
	}
}
