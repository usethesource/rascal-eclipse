package org.rascalmpl.eclipse.perspective.actions;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IResource;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.rascalmpl.eclipse.library.util.Clipboard;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;

public class ResourceContributionItem extends ContributionItem {

  public ResourceContributionItem() {
    // TODO Auto-generated constructor stub
  }

  public ResourceContributionItem(String id) {
    super(id);
    // TODO Auto-generated constructor stub
  }
  
  @Override
  public void fill(Menu menu, int index) {
   
    final IValueFactory vf = ValueFactoryFactory.getValueFactory();
    final Clipboard cb = new Clipboard(vf);
    new ActionContributionItem(new Action("Copy source location") { 
      @Override
      public void run() {
        try {
          final IResource val = getSelectedValue();
          
          if (val != null) {
            URI uri = URIUtil.create("project", val.getProject().getName(), "/" + val.getProjectRelativePath().toPortableString());
            cb.copy(vf.sourceLocation(uri));
          }
        } catch (URISyntaxException e) {
          // s
        }
      }
    }).fill(menu, index);
  }
  
  private IResource getSelectedValue() {
    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    
    if (window != null) {
        ISelection sel = window.getSelectionService().getSelection();
        
        if (sel instanceof IStructuredSelection) {
          IStructuredSelection selection = (IStructuredSelection) sel;
          Object firstElement = selection.getFirstElement();

          if (firstElement instanceof IResource) {
            return ((IResource) firstElement);
          }
        }
    }
    
    return null;
  }

}
