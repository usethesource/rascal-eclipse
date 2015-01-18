package org.rascalmpl.eclipse.navigator;

import java.net.URI;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.eclipse.uri.URIStorage;

public class NavigatorActionProvider extends CommonActionProvider {

  public class OpenFileStoreAction extends Action {
    private final IWorkbenchPage page;
    private final ISelectionProvider sp;
    private URIStorage store;

    public OpenFileStoreAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
      this.page = page;
      this.sp = selectionProvider;
    }
    
    @Override
    public String getText() {
      return "Open";
    }
    
    @Override
    public void run() {
      if (store != null) {
        if (!store.isDirectory()) {
        	EditorUtil.openAndSelectURI(store.getURI(), store.getRegistry());
        }
      }
    }
    
    public boolean isEnabled() {
      ISelection selection = sp.getSelection();
      if (!selection.isEmpty()) {
        IStructuredSelection sSelection = (IStructuredSelection) selection;
        if(sSelection.size() == 1 && sSelection.getFirstElement() instanceof URIStorage) {
          store = (URIStorage) sSelection.getFirstElement();
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
      openAction =  new OpenFileStoreAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
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
