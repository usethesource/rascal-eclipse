package org.meta_environment.rascal.eclipse.lib;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.type.TypeStore;
import org.meta_environment.ValueFactoryFactory;
import org.meta_environment.rascal.eclipse.Activator;
import org.meta_environment.rascal.interpreter.control_exceptions.Throw;

public class Resources {
	private static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	private static final TypeFactory TF = TypeFactory.getInstance();
	private static final IWorkspaceRoot ROOT = ResourcesPlugin.getWorkspace().getRoot();
	private static final TypeStore store = new TypeStore();
	private static final Type res = TF.abstractDataType(store, "Resource");
	private static final Type root = TF.constructor(store, res, "root", TF.setType(res), "projects");
	private static final Type project = TF.constructor(store, res, "project", TF.stringType(), "name", TF.setType(res), "contents");
	private static final Type folder = TF.constructor(store, res, "folder", TF.stringType(), "name", TF.setType(res), "contents");
	private static final Type file =  TF.constructor(store, res, "file", TF.stringType(), "name", TF.stringType(), "extension", TF.sourceLocationType(), "location");
	
	public static ISet projects() {
		IProject[] projects = ROOT.getProjects();
		ISetWriter w = VF.setWriter(TF.stringType());
		
		for (IProject p : projects) {
			w.insert(VF.string(p.getName()));
		}
		
		return w.done();
	}
	
	public static ISet references(IString name) {
		IProject project = getIProject(name.getValue());
		ISetWriter w = VF.setWriter(TF.stringType());
		
		try {
			for (IProject r : project.getReferencedProjects()) {
				w.insert(VF.string(r.getName()));
			}
		} catch (FactTypeUseException e) {
			// does not happen
		} catch (CoreException e) {
			// TODO what to do about this?
		}
		
		return w.done();
	}
	
	public static ISourceLocation location(IString name) {
		IProject project = getIProject(name.getValue());
		
		try {
			URL url = new URL("file://" + project.getLocation().toString());
			return VF.sourceLocation(url, 0, 0, 1, 1, 0, 0);
		} 
		catch (MalformedURLException e) {
			// this does not happen
			return null;
		}
	}
	
	public static ISet files(IString name) {
		IProject project = getIProject(name.getValue());
		final ISetWriter w = VF.setWriter(TF.sourceLocationType());
		
		try {
			project.accept(new IResourceVisitor() {

				public boolean visit(IResource resource)
						throws CoreException {
					if (resource.exists() && !resource.isDerived()) {
						if (resource instanceof IFile) {
							try {
								URL url = new URL("file://" + resource.getLocation().toString());
								w.insert(VF.sourceLocation(url, 0, 0, 1, 1, 0, 0));	
								
								return false;
							} catch (MalformedURLException e) {
								// does not happen
							}
						
						}
					}
					return true;
				}
			});
		} catch (FactTypeUseException e) {
			// does not happen
		} catch (CoreException e) {
			// TODO what to do about this?
		}
		
		return w.done();
	}
	
	public static IConstructor root() {
		ISetWriter projects = VF.setWriter(res);
		
		for (IProject p : ROOT.getProjects()) {
			ISet contents = getProjectContents(p);
			projects.insert(project.make(VF, VF.string(p.getName()), contents));
		}
		
		return (IConstructor) root.make(VF, projects.done());
	}
	
	public static IConstructor getProject(IString projectName) {
		IProject p = getIProject(projectName.getValue());
		ISet contents = getProjectContents(p);
		return (IConstructor) project.make(VF, VF.string(p.getName()), contents);
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
		return folder.make(VF, VF.string(resource.getName()), getFolderContents(resource));
	}

	private static IValue getFile(IFile resource) {
		IPath fullPath = resource.getFullPath();
		String fileExtension = resource.getFileExtension();
		try {
			URL url = new URL("file://" + resource.getLocation().toString());
			return file.make(VF, VF.string(fullPath.segment(fullPath.segmentCount() - 1)),
					VF.string(fileExtension == null ? "": fileExtension),
					VF.sourceLocation(url, 0, 0, 1, 1, 0, 0));
		}
		catch (MalformedURLException e) {
			// this does not happen
			return null;
		}
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
