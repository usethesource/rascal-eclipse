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
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.eclipse.library.util.Clipboard;
import org.rascalmpl.eclipse.navigator.NavigatorContentProvider.URIContent;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValueFactory;

public class NavigatorActionProvider extends CommonActionProvider {

  public static class OpenFileStoreAction extends Action {
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

  public static class CopySourceLocationAction extends Action {
      private final ISelectionProvider sp;
      private ISourceLocation store;
      private final IValueFactory vf = ValueFactoryFactory.getValueFactory();
      private final Clipboard cb = new Clipboard(vf);

      public CopySourceLocationAction(ISelectionProvider selectionProvider) {
        this.sp = selectionProvider;
      }
      
      @Override
      public String getText() {
        return "Copy Location";
      }
      
      @Override
      public void run() {
        if (store != null) {
            cb.copy(store);
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
  private CopySourceLocationAction copyAction;

  public NavigatorActionProvider() {     
  }

  public void init(ICommonActionExtensionSite aSite) {
    ICommonViewerSite viewSite = aSite.getViewSite();
    if (viewSite instanceof ICommonViewerWorkbenchSite) {
      ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewSite;
      openAction =  new OpenFileStoreAction(workbenchSite.getSelectionProvider());
      copyAction = new CopySourceLocationAction(workbenchSite.getSelectionProvider());
    }
  } 

  public void fillActionBars(IActionBars actionBars) { 
    if (openAction.isEnabled()) {
      actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
      actionBars.getMenuManager().add(openAction);
    }
    
    if (copyAction.isEnabled()) {
        actionBars.getMenuManager().add(copyAction);
    }
  }

  public void fillContextMenu(IMenuManager menu) {
    if (openAction.isEnabled()) {
      menu.add(openAction);
    }
    if (copyAction.isEnabled()) {
        menu.add(copyAction); 
    }
  }
}
