package org.rascalmpl.eclipse.navigator;

import java.text.Collator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.rascalmpl.eclipse.uri.URIStorage;

public class Sorter extends ViewerSorter {

  public Sorter() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public int category(Object element)
  {
	  if (element instanceof IFolder) {
		  return 1;
	  }
	  
	  if (element instanceof URIStorage) {
		  if (((URIStorage) element).isDirectory()) {
			  return 1;
		  }
	  }
	  
	  return 0;
  }
  
  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    return 0; // leave it to case insenstive compare of label string
  }
  
  public Sorter(Collator collator) {
    super(collator);
  }

}
