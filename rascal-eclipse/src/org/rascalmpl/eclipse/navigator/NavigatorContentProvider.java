package org.rascalmpl.eclipse.navigator;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.interpreter.load.RascalSearchPath;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIStorage;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.value.ISourceLocation;

public class NavigatorContentProvider implements ITreeContentProvider, IResourceChangeListener,
	IResourceDeltaVisitor {
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
          IResource[] members = project.members();
          Object[] result = new Object[members.length + 1];
          System.arraycopy(members, 0, result, 0, members.length);
          result[members.length] = new SearchPath(project);
          return result;
        }
        else if (project.isOpen()) {
          return project.members();
        }
      }
      else if (parentElement instanceof IContainer) {
        return ((IContainer) parentElement).members();
      }
      else if (parentElement instanceof SearchPath) {
    	  return ((SearchPath) parentElement).getSearchPath().toArray();
      }
      else if (parentElement instanceof URIContent) {
    	  URIContent storage = (URIContent) parentElement;
    	  if (storage.isDirectory()) {
    		  return storage.listEntries();
    	  }
      }
    } catch (CoreException e) {
    	Activator.log(e.getMessage(), e);
    }

    return new Object[] {};
  }

  public static class SearchPath {
	  private IProject project;

	  public SearchPath(IProject project) {
		  this.project = project;
	  }
	  
	  public List<URIContent> getSearchPath() {
		  RascalSearchPath resolver =  ProjectEvaluatorFactory.getInstance().getProjectSearchPath(project);
		  List<URIContent> result = new LinkedList<>();
		  
		  for (ISourceLocation root : resolver.collect()) {
			  result.add(new URIContent(root, project, true));
		  }

		  return result;
	  }

	  public IProject getProject() {
		  return project;
	  }
	  
	  @Override
	  public boolean equals(Object obj) {
		  if (obj instanceof SearchPath) {
			  return ((SearchPath) obj).project.getName().equals(project.getName());
		  }
		  return false;
	  }

	  public int hashCode() {
		  return project.hashCode();
	  };
  }
  
  public static class URIContent {
	  private final ISourceLocation uri;
	  private final IProject project;
	  private final boolean isRoot;
	  
	  public URIContent(ISourceLocation uri, IProject project, boolean isRoot) {
		  this.uri = uri;
		  this.project = project;
		  this.isRoot = isRoot;
	  }
	  
	  public boolean isRoot() {
		  return isRoot;
	  }
	  
	  public String getName() {
		  return URIUtil.getLocationName(uri);
	  }

	  public ISourceLocation getURI() {
		  return uri;
	  }

	  public IProject getProject() {
		  return project;
	  }
	  
	  public URIContent[] listEntries() {
		  try {
			  return Arrays.stream(URIResolverRegistry.getInstance().list(uri))
			      .filter(loc -> loc.getPath() == null || !loc.getPath().endsWith(".class"))
			      .map(loc -> new URIContent(loc, project, false))
			      .toArray(i -> new URIContent[i]);			 
		  } catch (IOException e) {
			  return new URIContent[0];
		  }
	  }

	  public boolean isDirectory() {
		  return URIResolverRegistry.getInstance().isDirectory(uri);
	  }
	  
	  public boolean exists() {
		  return URIResolverRegistry.getInstance().exists(uri);
	  }

	  public URIContent makeChild(String child) {
		  return new URIContent(URIUtil.getChildLocation(uri, child), project, false);
	  }
	  
	  @Override
	  public boolean equals(Object obj) {
		 if (obj instanceof URIContent) {
			 return ((URIContent) obj).project.getName().equals(project.getName())
					 && ((URIContent) obj).uri.equals(uri);
		 }
		 return false;
	  }
	  
	  @Override
	  public int hashCode() {
		  return 7 + 17 * project.hashCode() + 13 * uri.hashCode();
	  }
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
    else if (element instanceof SearchPath) {
    	return ((SearchPath) element).getProject();
    }
    else if (element instanceof URIStorage) {
    	// TODO: don't know yet
    	return null;
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
