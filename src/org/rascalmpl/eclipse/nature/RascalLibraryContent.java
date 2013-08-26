package org.rascalmpl.eclipse.nature;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.rascalmpl.eclipse.Activator;

public class RascalLibraryContent {
  private final IFileStore root;
  private final String name;

  public RascalLibraryContent(String name, IFileStore root) {
    this.root = root;
    this.name = name;
  }
  
  public IFileStore getRoot() {
    return root;
  }
  
  public String getName() {
    return name;
  }
  
  public Object[] getContent() {
    try {
      return root.childStores(EFS.NONE, null);
    } catch (CoreException e) {
      Activator.log(e.getMessage(), e);
      return new Object[] {};
    }
  }
  
  @Override
  public String toString() {
    return name;
  }

}
