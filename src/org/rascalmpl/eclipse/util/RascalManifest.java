package org.rascalmpl.eclipse.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.rascalmpl.eclipse.Activator;

public class RascalManifest {
  private static final String SOURCE = "Source";
  private static final String META_INF = "META-INF";
  private static final String META_INF_RASCAL_MF = META_INF + "/RASCAL.MF";
  private static final String MAIN_MODULE = "Main-Module";
  private static final String MAIN_FUNCTION = "Main-Function";

  public static List<String> getSourceRoots(IProject project) {
    return getAttributeList(project, SOURCE);
  }
  
  public static List<String> getSourceRoots(Bundle project) {
    return getAttributeList(project, SOURCE);
  }
  
  public static String getMainModule(IProject project) {
    String result = getAttribute(project, MAIN_MODULE);
    if (result == null) {
      result = "Plugin";
    }
    return result;
  }
  
  public static String getMainModule(Bundle project) {
    String result = getAttribute(project, MAIN_MODULE);
    if (result == null) {
      result = "Plugin";
    }
    return result;
  }
  
  public static String getMainFunction(IProject project) {
    String result = getAttribute(project, MAIN_FUNCTION);
    if (result == null) {
      result = "main";
    }
    return result;
  }
  
  public static String getMainFunction(Bundle project) {
    String result = getAttribute(project, MAIN_FUNCTION);
    if (result == null) {
      result = "main";
    }
    return result;
  }
  
  private static List<String> getAttributeList(IProject project, String label) {
    IFile rascalMF = project.getFile(new Path(META_INF_RASCAL_MF));
    
    try {
      if (rascalMF.exists()) {
        Manifest manifest = new Manifest(rascalMF.getContents());
        String source = manifest.getMainAttributes().getValue(label);
        return Arrays.<String>asList(trim(source.split(",")));
      }
    } catch (IOException | CoreException e) {
      Activator.log("unexpected exception while reading RASCAL.MF", e);
    }
    
    return null;
  }
  
  private static String getAttribute(Bundle bundle, String label) {
    URL rascalMF = bundle.getResource(META_INF_RASCAL_MF);
    
    if (rascalMF == null) {
      return null;
    }
    
    try {
      InputStream is = FileLocator.openStream(bundle, new Path(META_INF_RASCAL_MF), false);
      Manifest manifest = new Manifest(is);
      return manifest.getMainAttributes().getValue(label).trim();
    } 
    catch (IOException e) {
      Activator.log("unexpected exception while reading RASCAL.MF", e);
    }
    
    return null;
  }
  
  private static List<String> getAttributeList(Bundle bundle, String label) {
    URL rascalMF = bundle.getResource(META_INF_RASCAL_MF);
    
    if (rascalMF == null) {
      return null;
    }
    
    try {
      InputStream is = FileLocator.openStream(bundle, new Path(META_INF_RASCAL_MF), false);
      Manifest manifest = new Manifest(is);
      String source = manifest.getMainAttributes().getValue(label);
      return Arrays.<String>asList(trim(source.split(",")));
    } 
    catch (IOException e) {
      Activator.log("unexpected exception while reading RASCAL.MF", e);
    }
    
    return null;
  }
  
  private static String getAttribute(IProject project, String label) {
    IFile rascalMF = project.getFile(new Path(META_INF_RASCAL_MF));
    
    try {
      if (rascalMF.exists()) {
        Manifest manifest = new Manifest(rascalMF.getContents());
        return manifest.getMainAttributes().getValue(label).trim();
      }
    } catch (IOException | CoreException e) {
      Activator.log("unexpected exception while reading RASCAL.MF", e);
    }
    
    return null;
  }

  private static String[] trim(String[] elems) {
    for (int i = 0; i < elems.length; i++) {
      elems[i] = elems[i].trim();
    }
    return elems;
  }

  public static void createIfNotPresent(IProject project, String rascalSrc, String mainModule, String mainFunction) {
    try {
      IFolder folder = project.getFolder(META_INF);
      if (!folder.exists()) {
        folder.create(false, false, null);
      }
      
      IFile rascalMF = project.getFile(new Path(META_INF_RASCAL_MF)) ;
      if (!rascalMF.exists()) {
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(SOURCE, rascalSrc);
        mainAttributes.put(MAIN_MODULE, mainModule);
        mainAttributes.put(MAIN_FUNCTION, mainFunction);
        manifest.write(new FileOutputStream(rascalMF.getLocation().toOSString()));
      }
    } 
    catch (IOException | CoreException e) {
      Activator.log("could not create RASCAL.MF", e);
    }
  }
}
