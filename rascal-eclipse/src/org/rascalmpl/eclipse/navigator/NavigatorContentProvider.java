package org.rascalmpl.eclipse.navigator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.StreamSupport;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
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
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.vallang.IBool;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IDateTime;
import io.usethesource.vallang.IExternalValue;
import io.usethesource.vallang.IInteger;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.INode;
import io.usethesource.vallang.IRational;
import io.usethesource.vallang.IReal;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.visitors.IValueVisitor;

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
    return new Object[] { };
  }


  @Override
  public Object[] getChildren(Object parentElement) {
    try {
      if (parentElement instanceof IProject) {
        IProject project = (IProject) parentElement;

        if (project.isOpen() && project.hasNature(IRascalResources.ID_RASCAL_NATURE)) {
          return new Object[] { new SearchPath(project) };
        }
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
      else if (parentElement instanceof ValueContent) {
          ValueContent content = (ValueContent) parentElement;
          if (content.isDirectory()) {
              return content.listEntries();
          }
      }
    } catch (CoreException e) {
    	Activator.log(e.getMessage(), e);
    }

    return new Object[] {};
  }

  public static class SearchPath {
	  private final IProject project;
	  private final RascalSearchPath resolver;

	  public SearchPath(IProject project) {
		  this.project = project;
		  this.resolver =  ProjectEvaluatorFactory.getInstance().getProjectSearchPath(project);
	  }
	  
	  public List<URIContent> getSearchPath() {
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
  
  public static class ValueContent {
      private final IValue val;
      private final IProject project;
      private Object parent;

      public ValueContent(IValue val, IProject project, Object parent) {
          this.val = val;
          this.project = project;
          this.parent = parent;
      }
      
      @Override
      public int hashCode() {
          return val.hashCode() * 13 + parent.hashCode() * 17 + 13333331;
      }
      
      public Object getParent() {
        return parent;
      }
      
      public IValue getValue() {
        return val;
      }
      
      @Override
      public boolean equals(Object obj) {
          if (obj instanceof ValueContent) {
              return parent.equals(((ValueContent) obj).parent) && val.equals(((ValueContent) obj).val);
          }
          return false;
      }
      
      public IProject getProject() {
        return project;
      }
      
      public boolean isDirectory() {
          return val.accept(new IValueVisitor<Boolean, RuntimeException>() {

            @Override
            public Boolean visitBoolean(IBool arg0) throws RuntimeException {
                return false;
            }

            @Override
            public Boolean visitConstructor(IConstructor arg0) throws RuntimeException {
                return true;
            }

            @Override
            public Boolean visitDateTime(IDateTime arg0) throws RuntimeException {
                return false;
            }

            @Override
            public Boolean visitExternal(IExternalValue arg0) throws RuntimeException {
                return false;
            }

            @Override
            public Boolean visitInteger(IInteger arg0) throws RuntimeException {
                return false;
            }

            @Override
            public Boolean visitList(IList arg0) throws RuntimeException {
                return true;
            }

            @Override
            public Boolean visitListRelation(IList arg0) throws RuntimeException {
                return true;
            }

            @Override
            public Boolean visitMap(IMap arg0) throws RuntimeException {
                return true;
            }

            @Override
            public Boolean visitNode(INode arg0) throws RuntimeException {
                return true;
            }

            @Override
            public Boolean visitRational(IRational arg0) throws RuntimeException {
                return false;
            }

            @Override
            public Boolean visitReal(IReal arg0) throws RuntimeException {
                return false;
            }

            @Override
            public Boolean visitRelation(ISet arg0) throws RuntimeException {
                return false;
            }

            @Override
            public Boolean visitSet(ISet arg0) throws RuntimeException {
                return true;
            }

            @Override
            public Boolean visitSourceLocation(ISourceLocation arg0) throws RuntimeException {
                return false;
            }

            @Override
            public Boolean visitString(IString arg0) throws RuntimeException {
                return false;
            }

            @Override
            public Boolean visitTuple(ITuple arg0) throws RuntimeException {
                return true;
            }
              
          }).booleanValue();
      }
      
      public Object[] listEntries() {
          return val.accept(new IValueVisitor<Object[], RuntimeException>() {
              private Object[] empty = new Object[0];
              private IValueFactory vf = ValueFactoryFactory.getValueFactory();
              
            @Override
            public Object[] visitBoolean(IBool arg0) throws RuntimeException {
                return empty;
            }

            @Override
            public Object[] visitConstructor(IConstructor arg0) throws RuntimeException {
                return arg0.asWithKeywordParameters().getParameters().entrySet().stream()
                        .map(x -> new ValueContent(vf.node(x.getKey(), x.getValue()), project, this))
                        .toArray(Object[]::new);
            }

            @Override
            public Object[] visitDateTime(IDateTime arg0) throws RuntimeException {
                return empty;
            }

            @Override
            public Object[] visitExternal(IExternalValue arg0) throws RuntimeException {
                return empty;
            }

            @Override
            public Object[] visitInteger(IInteger arg0) throws RuntimeException {
                return empty;
            }

            @Override
            public Object[] visitList(IList arg0) throws RuntimeException {
                return StreamSupport.stream(arg0.spliterator(), false)
                        .map(x -> newChild(x))
                        .toArray(Object[]::new);
            }

            private Object newChild(IValue x) {
                return x.getType().isSourceLocation() ? new URIContent((ISourceLocation) x, project, true) : new ValueContent(x, project, this);
            }

            @Override
            public Object[] visitListRelation(IList arg0) throws RuntimeException {
                return visitList(arg0);
            }

            @Override
            public Object[] visitMap(IMap arg0) throws RuntimeException {
                return StreamSupport.stream(arg0.spliterator(), false)
                        .map(x -> new ValueContent(vf.node(x.toString(), arg0.get(x)), project, this))
                        .toArray(Object[]::new);
            }

            @Override
            public Object[] visitNode(INode arg0) throws RuntimeException {
                if (arg0.arity() == 1 && arg0.get(0) instanceof IList) {
                    return visitList((IList) arg0.get(0));
                }
                else {
                    return StreamSupport.stream(arg0.spliterator(), false)
                            .map(x -> newChild(x))
                            .toArray(Object[]::new);
                }
            }

            @Override
            public Object[] visitRational(IRational arg0) throws RuntimeException {
                return empty;
            }

            @Override
            public Object[] visitReal(IReal arg0) throws RuntimeException {
                return empty;
            }

            @Override
            public Object[] visitRelation(ISet arg0) throws RuntimeException {
                return visitSet(arg0);
            }

            @Override
            public Object[] visitSet(ISet arg0) throws RuntimeException {
                return StreamSupport.stream(arg0.spliterator(), false)
                        .map(x -> newChild(x))
                        .toArray(Object[]::new);
            }

            @Override
            public Object[] visitSourceLocation(ISourceLocation arg0) throws RuntimeException {
                return empty;
            }

            @Override
            public Object[] visitString(IString arg0) throws RuntimeException {
                return empty;
            }

            @Override
            public Object[] visitTuple(ITuple arg0) throws RuntimeException {
                return StreamSupport.stream(arg0.spliterator(), false).map(x -> new ValueContent(x, project, this)).toArray(Object[]::new);
            }
          });
      }
      
      public String getName() {
          return val.accept(new IValueVisitor<String, RuntimeException>() {

            @Override
            public String visitBoolean(IBool arg0) throws RuntimeException {
                return arg0.toString();
            }

            @Override
            public String visitConstructor(IConstructor arg0) throws RuntimeException {
                return arg0.getName();
            }

            @Override
            public String visitDateTime(IDateTime arg0) throws RuntimeException {
                return arg0.toString();
            }

            @Override
            public String visitExternal(IExternalValue arg0) throws RuntimeException {
                return arg0.toString();
            }

            @Override
            public String visitInteger(IInteger arg0) throws RuntimeException {
                return arg0.toString();
            }

            @Override
            public String visitList(IList arg0) throws RuntimeException {
                return "[" + arg0.length() + "]";
            }

            @Override
            public String visitListRelation(IList arg0) throws RuntimeException {
                return visitList(arg0);
            }

            @Override
            public String visitMap(IMap arg0) throws RuntimeException {
                return "(" + arg0.size() + ")";
            }

            @Override
            public String visitNode(INode arg0) throws RuntimeException {
                return arg0.getName();
            }

            @Override
            public String visitRational(IRational arg0) throws RuntimeException {
                return arg0.toString();
            }

            @Override
            public String visitReal(IReal arg0) throws RuntimeException {
                return arg0.toString();
            }

            @Override
            public String visitRelation(ISet arg0) throws RuntimeException {
               return visitSet(arg0);
            }

            @Override
            public String visitSet(ISet arg0) throws RuntimeException {
                return "{" + arg0.size() + "}";
            }

            @Override
            public String visitSourceLocation(ISourceLocation arg0) throws RuntimeException {
                return arg0.toString();
            }

            @Override
            public String visitString(IString arg0) throws RuntimeException {
                return arg0.getValue();
            }

            @Override
            public String visitTuple(ITuple arg0) throws RuntimeException {
                return "<>";
            }
          });
      }
  }
  
  public static class URIContent {
	  private static final URIResolverRegistry reg = URIResolverRegistry.getInstance();
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
	      ISourceLocation l = uri;
	      
	      if (isJarLoc(l)) {
	          try {
	              l = URIUtil.changePath(URIUtil.changeScheme(l, "jar+" + l.getScheme()), l.getPath() + "!/");
	          } catch (URISyntaxException e) {
	              Activator.log("navigator could not dive into jar", e);
	          }
	      }
	      return doList(l);
	  }

    private boolean isJarLoc(ISourceLocation l) {
        return l.getPath() != null && l.getPath().endsWith(".jar");
    }

    private URIContent[] doList(ISourceLocation l) {
        try {
	          return Arrays.stream(reg.list(l))
	                  .map(loc -> new URIContent(loc, project, false))
	                  .toArray(URIContent[]::new);			 
	      } catch (IOException e) {
	          Activator.log("could not list entries", e);
	          return new URIContent[0];
	      }
    }
	  
	  public boolean isDirectory() {
		  return reg.isDirectory(uri) || isJarLoc(uri);
	  }
	  
	  public boolean exists() {
		  return reg.exists(uri);
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
    else if (element instanceof ValueContent) {
        return ((ValueContent) element).getParent();
    }
    else if (element instanceof URIStorage) {
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
