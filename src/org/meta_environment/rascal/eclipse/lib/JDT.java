package org.meta_environment.rascal.eclipse.lib;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.meta_environment.ValueFactoryFactory;
import org.meta_environment.rascal.eclipse.lib.jdt.JDTImporter;
import org.meta_environment.rascal.interpreter.control_exceptions.Throw;
import org.meta_environment.rascal.interpreter.utils.RuntimeExceptionFactory;

public class JDT {
	
	private static final IWorkspaceRoot ROOT = ResourcesPlugin.getWorkspace().getRoot();
    private static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	
    private static IProject getProject(String project) {
    	IProject p = ROOT.getProject(project);
		
		if (p == null) {
			throw new Throw(VF.string("Project does not exist"), (ISourceLocation) null, null);
		}
		
		return p;
    }
    
	public static IMap extractClass(ISourceLocation loc) {
		URI uri = loc.getURI();
		
		if (!uri.getScheme().equals("project")) {
			throw RuntimeExceptionFactory.schemeNotSupported(loc, null, null);
		}
		
		IProject p = getProject(uri.getHost());
		
		if (uri.getPath().length() == 0) {
			throw new Throw(VF.string("URI is not a file"), (ISourceLocation) null, null);
		}
		
		return new JDTImporter().importFacts(p.getFile(uri.getPath()));
	}
}
