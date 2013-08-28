package org.rascalmpl.eclipse.debug.core.sourcelookup;

import java.util.ArrayList;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;

public class FileStoreSourceContainer implements ISourceContainer {
  private boolean fSubfolders = false;
  private IFileStore fRootFile = null;

  public FileStoreSourceContainer(IFileStore root, boolean subfolders) {
    fRootFile = root;
  }
  
  public Object[] findSourceElements(String name) throws CoreException {
    ArrayList<Object> sources = new ArrayList<>();

    // An IllegalArgumentException is thrown from the "getFile" method 
    // if the path created by appending the file name to the container 
    // path doesn't conform with Eclipse resource restrictions.
    // To prevent the interruption of the search procedure we check 
    // if the path is valid before passing it to "getFile".   

    IFileStore target = fRootFile.getFileStore(new Path(name));
    if (target.fetchInfo().exists()) {
      sources.add(target);
    }         

    //check sub-folders   
    if (sources.isEmpty() && fSubfolders && fRootFile.fetchInfo().isDirectory()) {
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
    IFileStore[] children = fRootFile.childStores(EFS.NONE, null);
    ISourceContainer[] conts = new ISourceContainer[children.length];
    
    for (int i=0; i < children.length; i++) {
      conts[i] = new FileStoreSourceContainer(children[i], fSubfolders);
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
