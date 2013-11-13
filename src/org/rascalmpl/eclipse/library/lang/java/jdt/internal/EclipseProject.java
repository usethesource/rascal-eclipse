package org.rascalmpl.eclipse.library.lang.java.jdt.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IMapWriter;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;


/**
 * This class provides access to the Eclipse Java Compiler and knows about the structure
 * and configuration of Eclipse Java projects
 */
public class EclipseProject {
  private final IValueFactory VF;

  public EclipseProject(IValueFactory vf) {
    this.VF = vf;
  }

  public ISet sourceRootsForProject(ISourceLocation loc) {
    try {
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(loc.getAuthority());
      if (project == null || !project.isOpen()) {
        throw RuntimeExceptionFactory.io(VF.string("project " + loc.getAuthority() + " could not be opened."), null, null);
      }
      
      IJavaProject jProject = JavaCore.create(project);
      ISetWriter result = VF.setWriter();
      
      for (IPackageFragmentRoot root : jProject.getAllPackageFragmentRoots()) {
        if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
          IResource resource = root.getResource();
          IPath path = resource.getProjectRelativePath();
          IProject thisProject = resource.getProject();
          if (thisProject == project) {
            result.insert(VF.sourceLocation(loc.getScheme(), loc.getAuthority(), path.toPortableString()));  
          }
        }
      }
     
      return result.done();
    } 
    catch (JavaModelException | URISyntaxException e) {
      throw RuntimeExceptionFactory.io(VF.string(e.getMessage()), null, null);
    } 
  }
  
  public ISet classPathForProject(ISourceLocation loc) {
    try {
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(loc.getAuthority());
      if (project == null || !project.isOpen()) {
        throw RuntimeExceptionFactory.io(VF.string("project " + loc.getAuthority() + " could not be opened."), null, null);
      }
      
      IJavaProject jProject = JavaCore.create(project);
      
      ISetWriter result = VF.setWriter();
      for (IPackageFragmentRoot root : jProject.getAllPackageFragmentRoots()) {
        if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
          IPath path = root.getPath();
          if (!root.isExternal()) {
        	  // Need to make the path for all non-external package fragment roots into absolute paths
        	  path = root.getResource().getLocation();
          }
          result.insert(VF.sourceLocation("file", "", path.toString()));
        }
        if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
        	IResource resource = root.getResource();
            IPath path = resource.getProjectRelativePath();
            IProject thisProject = resource.getProject();
            if (thisProject != project) {
          	  result.insert(VF.sourceLocation("project", thisProject.getName(), "/"+path.toPortableString()));
            }
        }
      }
     
      return result.done();
    } 
    catch (JavaModelException | URISyntaxException e) {
      throw RuntimeExceptionFactory.io(VF.string(e.getMessage()), null, null);
    } 
  }
  
  public IMap getProjectOptions(ISourceLocation loc) {
    URI uri = loc.getURI();
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getAuthority());
    if (project == null || !project.isOpen()) {
      throw RuntimeExceptionFactory.io(VF.string("project " + uri.getAuthority() + " could not be opened."), null, null);
    }

    IJavaProject jProject = JavaCore.create(project);
    IMapWriter result = VF.mapWriter();
    
    @SuppressWarnings("unchecked")
    Map<String,String> options = jProject.getOptions(true);
    
    for (Entry<String,String> entry : options.entrySet()) {
      result.put(VF.string(entry.getKey()), VF.string(entry.getValue()));
    }

    return result.done();
  }
}
