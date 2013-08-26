package org.rascalmpl.eclipse.navigator;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.rascalmpl.eclipse.Activator;

public class NavigatorActionProvider extends CommonActionProvider {

  public class OpenFileStoreAction extends Action {
    private final IWorkbenchPage page;
    private final ISelectionProvider sp;
    private IFileStore store;

    public OpenFileStoreAction(IWorkbenchPage page, ISelectionProvider selectionProvider) {
      this.page = page;
      this.sp = selectionProvider;
    }
    
    @Override
    public void run() {
      if (store != null) {
        try {
          IDE.openEditorOnFileStore(page, store);
        } catch (PartInitException e) {
          Activator.log("could not open editor for " + store, e);
        }
      }
    }
    
    public boolean isEnabled() {
      ISelection selection = sp.getSelection();
      if (!selection.isEmpty()) {
        IStructuredSelection sSelection = (IStructuredSelection) selection;
        if(sSelection.size() == 1 && sSelection.getFirstElement() instanceof IFileStore) {
          store = ((IFileStore)sSelection.getFirstElement());        
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
