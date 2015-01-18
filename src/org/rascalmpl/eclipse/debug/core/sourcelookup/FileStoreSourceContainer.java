package org.rascalmpl.eclipse.debug.core.sourcelookup;

import java.util.ArrayList;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.rascalmpl.eclipse.uri.URIStorage;

public class FileStoreSourceContainer implements ISourceContainer {
  private boolean fSubfolders = false;
  private URIStorage fRootFile = null;

  public FileStoreSourceContainer(URIStorage root, boolean subfolders) {
    fRootFile = root;
  }
  
  public Object[] findSourceElements(String name) throws CoreException {
    ArrayList<Object> sources = new ArrayList<>();

    // An IllegalArgumentException is thrown from the "getFile" method 
    // if the path created by appending the file name to the container 
    // path doesn't conform with Eclipse resource restrictions.
    // To prevent the interruption of the search procedure we check 
    // if the path is valid before passing it to "getFile".   

    URIStorage needle = fRootFile.makeChild(name);
    if (needle.exists()) {
      sources.add(needle);
    }         

    //check sub-folders   
    if (sources.isEmpty() && fSubfolders && fRootFile.isDirectory()) {
      ISourceContainer[] children = getSourceContainers();
         
      for (ISourceContainer child : children) {
        Object[] objects = child.findSourceElements(name);
        if (objects == null || objects.length == 0) {
          continue;
        }
        else {
          sources.add(objects[0]);
          break;
        }
      }
    }     
    
    if(sources.isEmpty()) {
      return new Object[0];
    }
    return sources.toArray();
  }
  
  public String getName() {   
    return fRootFile.getName(); 
  }

  public boolean equals(Object obj) {
    if (obj != null && obj instanceof FileStoreSourceContainer) {
      FileStoreSourceContainer loc = (FileStoreSourceContainer) obj;
      return loc.fRootFile.equals(fRootFile);
    } 
    return false;
  } 
  
  public int hashCode() {
    return fRootFile.hashCode();
  }

  public boolean isComposite() {  
    return fSubfolders;
  }

  @Override
  public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
    return null;
  }

  @Override
  public void init(ISourceLookupDirector director) {
    
  }

  @Override
  public ISourceContainer[] getSourceContainers() throws CoreException {
    String[] children = fRootFile.listEntries();
    ISourceContainer[] conts = new ISourceContainer[children.length];
    
    for (int i=0; i < children.length; i++) {
      conts[i] = new FileStoreSourceContainer(fRootFile.makeChild(children[i]), fSubfolders);
    }
    
    return conts;
  }

  @Override
  public ISourceContainerType getType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void dispose() {
    // TODO Auto-generated method stub
    
  }
}
