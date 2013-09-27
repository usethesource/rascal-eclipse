package org.rascalmpl.eclipse.library.lang.java.jdt.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
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
import org.rascalmpl.uri.URIUtil;


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
      URI uri = loc.getURI();
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getAuthority());
      if (project == null || !project.isOpen()) {
        throw RuntimeExceptionFactory.io(VF.string("project " + uri.getAuthority() + " could not be opened."), null, null);
      }
      
      IJavaProject jProject = JavaCore.create(project);
      ISetWriter result = VF.setWriter();
      
      for (IPackageFragmentRoot root : jProject.getAllPackageFragmentRoots()) {
        if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
          IPath path = root.getResource().getProjectRelativePath();
          URI rootUri = URIUtil.changePath(uri, "/" + path.toPortableString());
          result.insert(VF.sourceLocation(rootUri));
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
      URI uri = loc.getURI();
      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.getAuthority());
      if (project == null || !project.isOpen()) {
        throw RuntimeExceptionFactory.io(VF.string("project " + uri.getAuthority() + " could not be opened."), null, null);
      }
      
      IJavaProject jProject = JavaCore.create(project);
      
      ISetWriter result = VF.setWriter();
      
      for (IPackageFragmentRoot root : jProject.getAllPackageFragmentRoots()) {
        if (root.getKind() == IPackageFragmentRoot.K_BINARY) {
          IPath path = root.getPath();
          String pathString = path.toPortableString();
          URI rootUri = URIUtil.create("file", "", pathString.startsWith("/") ? pathString : "/" + pathString);
          result.insert(VF.sourceLocation(rootUri));
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
