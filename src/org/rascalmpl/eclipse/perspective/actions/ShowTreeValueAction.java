package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.ui.tree.Editor;
import org.eclipse.jface.action.IAction;

public class ShowTreeValueAction extends AbstractIValueAction {
  @Override
  public void run(IAction action) {
    IValue v = getValue();
    
    if (v != null) {
      Editor.open(getValue());
    }
  }
}
