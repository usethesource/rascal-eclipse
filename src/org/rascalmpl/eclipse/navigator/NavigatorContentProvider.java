package org.rascalmpl.eclipse.navigator;

import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;

public class NavigatorContentProvider implements ITreeContentProvider {
  private Map<IFileStore,RascalLibraryContent> libraries;

  @Override
  public void dispose() {
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  @Override
  public Object[] getElements(Object inputElement) {
    try {
      if (inputElement instanceof IProject) {
        return getProjectSearchPath();
      }
      else if (inputElement instanceof IFileStore) {
        return ((IFileStore) inputElement).childStores(EFS.NONE, null);
      }
      else if (inputElement instanceof RascalLibraryContent) {
        return ((RascalLibraryContent) inputElement).getContent();
      }
    } 
    catch (CoreException e) {
      Activator.log(e.getMessage(), e);
    }
    
    return new Object[] { };
  }

  private Object[] getProjectSearchPath() {
    try {
      RascalLibraryFileSystem fd = RascalLibraryFileSystem.getInstance();
      Map<String, IFileStore> libs = fd.getRoots();
      Object[] roots = new Object[libs.size()];
      int i = 0;
      
      // TODO: add check if library is referenced in RASCAL.MF
      for (String key : libs.keySet()) {
        roots[i++] = new RascalLibraryContent(key, libs.get(key));
      }
      
      return roots;
    } catch (CoreException e) {
      Activator.log(e.getMessage(), e);
    }
    
    return new Object[] {};
  }

  @Override
  public Object[] getChildren(Object parentElement) {
    try {
      if (parentElement instanceof IProject) {
        IProject project = (IProject) parentElement;

        if (project.hasNature(IRascalResources.ID_RASCAL_NATURE)) {
          return getProjectSearchPath();
        }
      }
      else if (parentElement instanceof IFileStore) {
        return ((IFileStore) parentElement).childStores(EFS.NONE, null);
      }
      else if (parentElement instanceof RascalLibraryContent) {
        return ((RascalLibraryContent) parentElement).getContent();
      }
    } catch (CoreException e) {
      Activator.log(e.getMessage(), e);
    }

    return new Object[] {};
  }

  @Override
  public Object getParent(Object element) {
    if (element instanceof IResource) {
      return ((IResource) element).getParent();
    } 
    else if (element instanceof IFileStore) {
      IFileStore parent = ((IFileStore) element).getParent();
      if (libraries.containsKey(parent)) {
        return libraries.get(parent);
      }
    }
    
    return null;
  }

  @Override
  public boolean hasChildren(Object element) {
    return getChildren(element).length > 0;
  }

}
