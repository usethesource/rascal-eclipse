package org.rascalmpl.eclipse.navigator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.eclipse.navigator.NavigatorContentProvider.URIContent;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.value.ISourceLocation;

public class NavigatorActionProvider extends CommonActionProvider {

  public class OpenFileStoreAction extends Action {
    private final ISelectionProvider sp;
    private ISourceLocation store;

    public OpenFileStoreAction(ISelectionProvider selectionProvider) {
      this.sp = selectionProvider;
    }
    
    @Override
    public String getText() {
      return "Open";
    }
    
    @Override
    public void run() {
      if (store != null) {
        if (!URIResolverRegistry.getInstance().isDirectory(store)) {
        	EditorUtil.openAndSelectURI(store);
        }
      }
    }
    
    public boolean isEnabled() {
      ISelection selection = sp.getSelection();
      if (!selection.isEmpty()) {
        IStructuredSelection sSelection = (IStructuredSelection) selection;
        if(sSelection.size() == 1 && sSelection.getFirstElement() instanceof URIContent) {
          store = ((URIContent) sSelection.getFirstElement()).getURI();
          return true;
        }
      }
      return false;
    }

  }

  private OpenFileStoreAction openAction;

  public NavigatorActionProvider() {     
  }

  public void init(ICommonActionExtensionSite aSite) {
    ICommonViewerSite viewSite = aSite.getViewSite();
    if (viewSite instanceof ICommonViewerWorkbenchSite) {
      ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewSite;
      openAction =  new OpenFileStoreAction(workbenchSite.getSelectionProvider());
    }
  } 

  public void fillActionBars(IActionBars actionBars) { 
    if (openAction.isEnabled()) {
      actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
    }
  }

  public void fillContextMenu(IMenuManager menu) {
    if (openAction.isEnabled()) {
      menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openAction);
    }
  }
}
