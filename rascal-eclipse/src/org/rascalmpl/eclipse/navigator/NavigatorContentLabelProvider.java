package org.rascalmpl.eclipse.navigator;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.rascalmpl.eclipse.navigator.NavigatorContentProvider.SearchPath;
import org.rascalmpl.eclipse.navigator.NavigatorContentProvider.URIContent;
import org.rascalmpl.eclipse.navigator.NavigatorContentProvider.ValueContent;

public class NavigatorContentLabelProvider extends JavaElementLabelProvider {
  @Override
  public String getText(Object element) {
    if (element instanceof URIContent) {
    	URIContent store = (URIContent) element;
    	return store.isRoot() ? store.getURI().toString() : store.getName();
    }
    else if (element instanceof SearchPath) {
    	return "search path";
    }
    else if (element instanceof ValueContent) {
        return ((ValueContent) element).getName();
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
    else if (element instanceof URIContent) {
        return new StyledString(getText(element));
    }  
    else if (element instanceof SearchPath || element instanceof ValueContent) {
        return new StyledString(getText(element));
    }
    
    return super.getStyledText(element);
  }
  
  @Override
  public Image getImage(Object element) {
	  if (element instanceof SearchPath || element instanceof ValueContent) {
		  return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_JAR_WITH_SOURCE);
	  }
	  if (element instanceof URIContent) {
		  URIContent curr = (URIContent) element;

		  if (curr.isDirectory()) {
			  if (curr.isRoot()) {
				  return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_JAR_WITH_SOURCE);
			  }
			  else {
				  return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
			  }
		  }

		  return PlatformUI.getWorkbench().getSharedImages().getImage(org.eclipse.ui.ISharedImages.IMG_OBJ_FILE);
	  }
    
	  return super.getImage(element);
  }
}
