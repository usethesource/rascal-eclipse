package org.meta_environment.rascal.eclipse.lib;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.type.TypeStore;
import org.meta_environment.ValueFactoryFactory;
import org.meta_environment.rascal.eclipse.Activator;
import org.meta_environment.rascal.interpreter.control_exceptions.Throw;
import org.meta_environment.rascal.interpreter.utils.RuntimeExceptionFactory;

public class Resources {
	private static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	private static final TypeFactory TF = TypeFactory.getInstance();
	private static final IWorkspaceRoot ROOT = ResourcesPlugin.getWorkspace().getRoot();
	private static final TypeStore store = new TypeStore();
	private static final Type res = TF.abstractDataType(store, "Resource");
	private static final Type root = TF.constructor(store, res, "root", TF.setType(res), "projects");
	private static final Type project = TF.constructor(store, res, "project", TF.sourceLocationType(), "id", TF.setType(res), "contents");
	private static final Type folder = TF.constructor(store, res, "folder", TF.sourceLocationType(), "id", TF.setType(res), "contents");
	private static final Type file =  TF.constructor(store, res, "file", TF.sourceLocationType(), "id");
	
	public static ISet projects() {
		IProject[] projects = ROOT.getProjects();
		ISetWriter w = VF.setWriter(TF.sourceLocationType());
		
		for (IProject p : projects) {
			w.insert(makeProject(p));
		}
		
		return w.done();
	}
	
	public static ISet references(ISourceLocation loc) {
		IProject project = getIProject(loc.getURI().getHost());
		ISetWriter w = VF.setWriter(TF.sourceLocationType());
		
		try {
			for (IProject r : project.getReferencedProjects()) {
				w.insert(makeProject(r));
			}
		} 
		catch (CoreException e) {
			// TODO what to do about this?
		}
		
		return w.done();
	}

	public static ISourceLocation makeProject(IProject r) {
		try {
			return VF.sourceLocation(new URI("project", r.getName(), "", null));
		} catch (URISyntaxException e) {
			// won't happen
			throw RuntimeExceptionFactory.malformedURI("project://" + r.getName(), null, null);
		}
	}
	
	public static ISourceLocation location(ISourceLocation name) {
		IProject project = getIProject(name.getURI().getHost());
		String path = name.getURI().getPath();
		
		if (path != null && path.length() != 0) {
			IFile file = project.getFile(path);
			return VF.sourceLocation(file.getLocation().toString().replaceAll(" ", "%20"));
		}
		
		return VF.sourceLocation(project.getLocationURI());
	}
	
	public static ISet files(ISourceLocation name) {
		final String projectName = name.getURI().getHost();
		IProject project = getIProject(projectName);
		final ISetWriter w = VF.setWriter(TF.sourceLocationType());
		try {
			project.accept(new IResourceVisitor() {
				public boolean visit(IResource resource)
						throws CoreException {
					if (resource.exists() && !resource.isDerived()) {
						if (resource instanceof IFile) {
							w.insert(makeFile(resource));	
						}
					}
					return true;
				}

				
			});
		}
		catch (CoreException e) {
			// TODO what to do about this?
		}
		
		return w.done();
	}
	
	public static ISourceLocation makeFile(IResource resource) {
		String path = resource.getProjectRelativePath().toString();
		path = path.startsWith("/") ? path : "/" + path;
		try {
			return VF.sourceLocation(new URI("project", resource.getProject().getName(), path.replaceAll(" ", "%20"), null));
		} catch (URISyntaxException e) {
			throw RuntimeExceptionFactory.malformedURI("project://" + resource.getProject().getName() + path.replaceAll(" ", "%20"), null, null);
		}
	}
	
	public static ISourceLocation makeFolder(IFolder folder) {
		String path = folder.getProjectRelativePath().toString();
		path = path.startsWith("/") ? path : "/" + path;
		try {
			return VF.sourceLocation(new URI("project", folder.getProject().getName(), path.replaceAll(" ", "%20"), null));
		} catch (URISyntaxException e) {
			throw RuntimeExceptionFactory.malformedURI("project://" + folder.getProject().getName() + path.replaceAll(" ", "%20"), null, null);
		}
	}
	
	public static IConstructor root() {
		ISetWriter projects = VF.setWriter(res);
		
		for (IProject p : ROOT.getProjects()) {
			ISet contents = getProjectContents(p);
			projects.insert(project.make(VF, makeProject(p), contents));
		}
		
		return (IConstructor) root.make(VF, projects.done());
	}
	
	public static IConstructor getProject(ISourceLocation projectName) {
		IProject p = getIProject(projectName.getURI().getHost());
		ISet contents = getProjectContents(p);
		return (IConstructor) project.make(VF, projectName, contents);
	}
	
	private static IProject getIProject(String projectName) {
		IProject p = ROOT.getProject(projectName);
		if (p != null) {
			return p;
		}
		throw new Throw(VF.string("Project does not exist: " + projectName), (ISourceLocation) null, null);
	}
	
	private static ISet getProjectContents(IProject project) {
		final ISetWriter w = VF.setWriter(res);

		try {
			project.accept(new IResourceVisitor() {

				public boolean visit(IResource resource)
						throws CoreException {
					if (resource instanceof IFile) {
						w.insert(getFile((IFile) resource));
						return false;
					}
					else if (resource instanceof IFolder) {
						w.insert(getFolder((IFolder) resource));
						return false;
					}
					return true;
				}
			}, IResource.DEPTH_ONE, false);
		} catch (FactTypeUseException e) {
			Activator.getInstance().logException("root", e);
		} catch (CoreException e) {
			Activator.getInstance().logException("root", e);
		}
		
		return w.done();
	}
	
	private static IValue getFolder(IFolder resource) {
		return folder.make(VF, makeFolder(resource), getFolderContents(resource));
	}

	private static IValue getFile(IFile resource) {
		return file.make(VF, makeFile(resource));
	}

	private static ISet getFolderContents(final IFolder folder) {
	final ISetWriter w = VF.setWriter(res);
		
		try {
			folder.accept(new IResourceVisitor() {

				public boolean visit(IResource resource)
						throws CoreException {
					if (resource == folder) {
						return true;
					}
					if (resource instanceof IFile) {
						w.insert(getFile((IFile) resource));
						return false;
					}
					else if (resource instanceof IFolder) {
						w.insert(getFolder((IFolder) resource));
						return false;
					}
					return true;
				}
			}, IResource.DEPTH_ONE, false);
		} catch (FactTypeUseException e) {
			// does not happen
		} catch (CoreException e) {
			// TODO what to do about this?
		}
		
		return w.done();
	}

}
