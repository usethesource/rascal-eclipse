package org.rascalmpl.eclipse.library.lang.java.jdt.internal;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.rascalmpl.interpreter.utils.RuntimeExceptionFactory;
import org.rascalmpl.value.IMap;
import org.rascalmpl.value.IMapWriter;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.ISetWriter;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IValueFactory;


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
          IProject thisProject = resource.getProject();
          if (thisProject == project) {
            result.insert(VF.sourceLocation("file", "", resource.getLocation().toString()));  
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
            IProject thisProject = resource.getProject();
            if (thisProject != project) {
          	  result.insert(VF.sourceLocation("file", "", resource.getLocation().toString()));
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
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(loc.getAuthority());
    if (project == null || !project.isOpen()) {
      throw RuntimeExceptionFactory.io(VF.string("project " + loc.getAuthority() + " could not be opened."), null, null);
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
