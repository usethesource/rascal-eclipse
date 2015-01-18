package org.rascalmpl.eclipse.navigator;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
//import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.rascalmpl.eclipse.navigator.NavigatorContentProvider.SearchPath;
import org.rascalmpl.eclipse.uri.URIStorage;

public class NavigatorContentLabelProvider extends JavaElementLabelProvider {
  private final WorkbenchLabelProvider wbProvider = new WorkbenchLabelProvider();
  
  @Override
  public String getText(Object element) {
    if (element instanceof URIStorage) {
      URIStorage store = (URIStorage) element;
      return store.isRoot() ? store.getURI().toString() : store.getName();
    }
    else if (element instanceof SearchPath) {
    	return "search path";
    }
    else {
      return super.getText(element);
    }
  }
  
  @Override
  public StyledString getStyledText(Object element) {
    if (element instanceof IFileStore) {
      return new StyledString(getText(element));
    }
    else if (element instanceof URIStorage) {
        return new StyledString(getText(element));
    }  
    else if (element instanceof SearchPath) {
        return new StyledString(getText(element));
    }
    
    return super.getStyledText(element);
  }
  
  @Override
  public Image getImage(Object element) {
	  if (element instanceof SearchPath) {
		  return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_JAR_WITH_SOURCE);
	  }
	  if (element instanceof URIStorage) {
		  URIStorage curr = (URIStorage) element;

		  if (curr.isDirectory()) {
			  return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
		  }

		  return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FILE);
	  }
    
	  return wbProvider.getImage(element);
  }
}
