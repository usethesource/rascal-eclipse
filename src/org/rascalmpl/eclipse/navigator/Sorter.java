package org.rascalmpl.eclipse.navigator;

import java.text.Collator;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class Sorter extends ViewerSorter {

  public Sorter() {
    // TODO Auto-generated constructor stub
  }

  public int category(Object element)
  {
    if (element instanceof IFileStore) {
      if (((IFileStore) element).fetchInfo().isDirectory()) {
        return 1;
      }
    }
    return 0;
  }
  
  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    return e1.toString().compareTo(e2.toString());
  }
  
  public Sorter(Collator collator) {
    super(collator);
  }

}
