package org.rascalmpl.eclipse.navigator;

import java.net.URI;
import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.navigator.NavigatorContentProvider.SearchPath;
import org.rascalmpl.eclipse.navigator.NavigatorContentProvider.URIContent;
import org.rascalmpl.uri.URIResourceResolver;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.values.ValueFactoryFactory;

public class RascalNavigator extends CommonNavigator {
	
	public RascalNavigator() {
		super();
	}
	
	private void restoreState() {
		if (memento == null) {
			return;
		}

		ArrayList<Object> elements = new ArrayList<>();
		IContainer container = ResourcesPlugin.getWorkspace().getRoot();
		IMemento childMem = memento.getChild("expanded");
		if (childMem != null) {
			IMemento[] elementMem = childMem.getChildren("ws_element");
			for (int i = 0; i < elementMem.length; i++) {
				Object element = PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(elementMem[i].getString("name"));
				if (element != null) {
					elements.add(element);
				}
			}
			elementMem = childMem.getChildren("element");
			for (int i = 0; i < elementMem.length; i++) {
				Object element = container.findMember(elementMem[i].getString("path"));
				if (element != null) {
					elements.add(element);
				}
			}
			
			elementMem = childMem.getChildren("uri");
			for (int i = 0; i < elementMem.length; i++) {
				String strURI = elementMem[i].getString("uri");
				String strProject = elementMem[i].getString("project");
		
				if (strURI != null && strProject != null) {
					URI element = URIUtil.assumeCorrect(strURI);
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(strProject);			
					elements.add(new URIContent(ValueFactoryFactory.getValueFactory().sourceLocation(element), project, false));
				}
			}
			
			elementMem = childMem.getChildren("searchpath");
			for (int i = 0; i < elementMem.length; i++) {
				String strProject = elementMem[i].getString("project");
		
				if (strProject != null) {
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(strProject);			
					elements.add(new SearchPath(project));
				}
			}
		}
		getCommonViewer().setExpandedElements(elements.toArray());
	}
	  
	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		Object expandedElements[] = ((TreeViewer)this.getCommonViewer()).getExpandedElements();
		if (expandedElements.length > 0) {
			IMemento expandedMem = memento.createChild("expanded");
			for (int i = 0; i < expandedElements.length; i++) {
				if (expandedElements[i] instanceof IResource) {
					IMemento elementMem = expandedMem.createChild("element");
					elementMem.putString("path", ((IResource) expandedElements[i]).getFullPath().toString());
				}
				else if (expandedElements[i] instanceof URIContent) {
					IMemento elementMem = expandedMem.createChild("uri");
					elementMem.putString("uri", ((URIContent) expandedElements[i]).getURI().getURI().toString());
					elementMem.putString("project", ((URIContent) expandedElements[i]).getProject().getName());
				}
				else if (expandedElements[i] instanceof SearchPath) {
					IMemento elementMem = expandedMem.createChild("searchpath");
					elementMem.putString("project", ((SearchPath) expandedElements[i]).getProject().getName());
				}
				else if (expandedElements[i] instanceof IWorkingSet) {
					IMemento elementMem = expandedMem.createChild("ws_element");
					elementMem.putString("name", ((IWorkingSet) expandedElements[i]).getName());
				}
			}
		}
	}

	@Override
	public void createPartControl(Composite aParent) {
		super.createPartControl(aParent);
		restoreState();
	}

	public void reveal(ISourceLocation uri) {
		try {
			IResource handle = URIResourceResolver.getResource(uri);
			
			if (handle != null) {
				getCommonViewer().reveal(handle);
				getCommonViewer().setSelection(new StructuredSelection(handle));
			}
			else {
				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					if (project.hasNature(IRascalResources.ID_RASCAL_NATURE)) {
						URIContent content = new URIContent(uri, project, false);
						getCommonViewer().reveal(content);
						getCommonViewer().setSelection(new StructuredSelection(content));
						break;
					}
				}
			}
		}
		catch (CoreException e) {
			Activator.log("could not reveal " + uri, e);
		}
	}
}
