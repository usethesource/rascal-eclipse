package org.rascalmpl.eclipse.navigator;

import java.util.ArrayList;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;

public class RascalNavigator extends CommonNavigator {
	
	public RascalNavigator() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	private void restoreState() {
		@SuppressWarnings("rawtypes")
		ArrayList elements = new ArrayList();
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
				Object element = container.findMember(elementMem[i]
						.getString("path"));
	            if (element != null) {
	              elements.add(element);
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
	                  IMemento elementMem = expandedMem
	                          .createChild("element");
	                  elementMem.putString("path",
	                          ((IResource) expandedElements[i]).getFullPath()
	                                  .toString());
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
}
