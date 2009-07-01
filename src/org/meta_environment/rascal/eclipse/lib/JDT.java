package org.meta_environment.rascal.eclipse.lib;

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

public class JDT {
	
	private static final IWorkspaceRoot ROOT = ResourcesPlugin.getWorkspace().getRoot();
    private static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	
    private static IProject getProject(IString project) {
    	IProject p = ROOT.getProject(project.getValue());
		
		if (p == null) {
			throw new Throw(VF.string("Project does not exist"), (ISourceLocation) null, null);
		}
		
		return p;
    }

	private static IMap importFacts(IFile f) {
		JDTImporter importer = new JDTImporter();
		return importer.importFacts(f);
	}
    
	public static IMap extractFacts(IString project, IString file) {
		IProject p = getProject(project);
		IFile f = p.getFile(file.getValue());
		
		if (f.exists() && f.getProject() == p) {
			return importFacts(f);
		}

		throw new Throw(VF.string("File does not exist"), (ISourceLocation) null, null);
	}
	
	public static IMap extractFacts(IString project, ISourceLocation file) {
		IProject p = getProject(project);
		IFile f = ROOT.getFileForLocation(new Path(file.getURL().getPath()));
		
		if (f.exists() && f.getProject() == p) {
			return importFacts(f);
		}

		throw new Throw(VF.string("File does not exist"), (ISourceLocation) null, null);
	}
}
