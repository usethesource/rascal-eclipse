package org.rascalmpl.eclipse.navigator;

import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;

public class NavigatorContentProvider implements ITreeContentProvider, IResourceChangeListener,
	IResourceDeltaVisitor {
  private Map<IFileStore,RascalLibraryContent> libraries;
  public TreeViewer _viewer;

  public NavigatorContentProvider() {
	  super();
	  ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
  }
  
  @Override
  public void dispose() {
	  ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	  _viewer = (TreeViewer) viewer;
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (inputElement instanceof IWorkspaceRoot) {
      IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
      IWorkingSet[] workingSets = manager.getWorkingSets();
      
      if (workingSets.length == 0) {
        return ((IWorkspaceRoot) inputElement).getProjects();
      }
      else {
        return workingSets;
      }
    }
    else if (inputElement instanceof IContainer) {
      try {
        return ((IContainer) inputElement).members();
      } catch (CoreException e) {
        Activator.log("navigator exception", e);
      }
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
      if (parentElement instanceof IWorkspaceRoot) {
        IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
        return manager.getWorkingSets();
      }
      else if (parentElement instanceof IWorkingSet) {
        IAdaptable[] elems = ((IWorkingSet) parentElement).getElements();
        IResource[] resources = new IResource[elems.length];
        
        for (int i = 0, j = 0; i < elems.length; i++) {
          Object ad = elems[i].getAdapter(IResource.class);
          if (ad != null && ad instanceof IResource) {
            resources[j++] = (IResource) ad;
          }
        }
        
        return resources;
      }
      else if (parentElement instanceof IProject) {
        IProject project = (IProject) parentElement;

        if (project.isOpen() && project.hasNature(IRascalResources.ID_RASCAL_NATURE)) {
          Object[] libraries = getProjectSearchPath();
          IResource[] members = project.members();
          Object[] result = new Object[members.length + libraries.length];
          
          System.arraycopy(members, 0, result, 0, members.length);
          System.arraycopy(libraries, 0, result, members.length, libraries.length);
          
          return result;
        }
        else if (project.isOpen()) {
          return project.members();
        }
      }
      else if (parentElement instanceof IContainer) {
        return ((IContainer) parentElement).members();
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
    if (element instanceof IWorkingSet) {
      ResourcesPlugin.getWorkspace().getRoot();
    }
    else if (element instanceof IProject) {
      IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
      for (IWorkingSet set : manager.getWorkingSets()) {
        IAdaptable elems[] = set.getElements();
        for (int i = 0; i < elems.length; i++) {
          if (element == elems[i]) {
            return set;
          }
        }
      }
    }
    else if (element instanceof IResource) {
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

  @Override
  public void resourceChanged(IResourceChangeEvent event) {
	IResourceDelta delta = event.getDelta();
	try {
		delta.accept(this);
	} catch (CoreException e) { 
		e.printStackTrace();
	}
  }

  @Override
  public boolean visit(IResourceDelta delta) throws CoreException {
	  final IResource source = delta.getResource();
	  new UIJob("Refresh viewer") {  //$NON-NLS-1$
		public IStatus runInUIThread(IProgressMonitor monitor) {
		if (_viewer != null && !_viewer.getControl().isDisposed())
			_viewer.refresh(source);
		return Status.OK_STATUS;						
		}
	  }.schedule();
	  return false;
  }
}
