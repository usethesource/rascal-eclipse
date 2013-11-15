package org.rascalmpl.eclipse.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.interpreter.utils.RascalManifest;

/**
 * See @link {@link RascalManifest}. This class adds support for Eclipse projects 
 * and OSGI bundles.
 */
public class RascalEclipseManifest extends RascalManifest {

  public List<String> getSourceRoots(IProject project) {
    return getSourceRoots(manifest(project));
  }
  
  public List<String> getSourceRoots(Bundle project) {
    return getSourceRoots(manifest(project));
  }
  
  public String getMainModule(IProject project) {
    return getMainModule(manifest(project));
  }
  
  public String getMainModule(Bundle project) {
    return getMainModule(manifest(project));
  }
  
  public String getMainFunction(IProject project) {
    return getMainFunction(manifest(project));
  }
  
  public String getMainFunction(Bundle project) {
    return getMainFunction(manifest(project));
  }
  
  private InputStream manifest(Bundle bundle) {
    URL rascalMF = bundle.getResource(META_INF_RASCAL_MF);

    try {
      if (rascalMF != null) {
        return FileLocator.openStream(bundle, new Path(META_INF_RASCAL_MF), false);
      }
    }
    catch (IOException e) {
      // do nothing
    }
    
    return null;
  }
  
  private static InputStream manifest(IProject project) {
    IFile rascalMF = project.getFile(new Path(META_INF_RASCAL_MF));
    try {
      if (rascalMF.exists()) {
        return new BufferedInputStream(rascalMF.getContents());
      }
    } 
    catch (CoreException e) {
      // do nothing
    }
    
    return null;
  }
  
  public void createIfNotPresent(IProject project) {
    try {
      IFolder folder = project.getFolder(META_INF);
      if (!folder.exists()) {
        if (!new File(folder.getLocation().toOSString()).mkdirs()) {
          Activator.log("could not mkdir META-INF", new IOException());
          return;
        }
      }
      
      IFile rascalMF = project.getFile(new Path(META_INF_RASCAL_MF)) ;
      if (!rascalMF.exists()) {
        try (FileOutputStream file = new FileOutputStream(rascalMF.getLocation().toOSString())) {
          getDefaultManifest().write(file);
        }
      }
      
      project.refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());
    } 
    catch (IOException | CoreException e) {
      Activator.log("could not create RASCAL.MF", e);
    }
  }

  public boolean hasManifest(IProject project) {
    return hasManifest(manifest(project));
  }
  
  public boolean hasManifest(Bundle bundle) {
    return hasManifest(manifest(bundle));
  }
}
