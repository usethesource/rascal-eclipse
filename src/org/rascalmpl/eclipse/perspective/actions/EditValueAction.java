package org.rascalmpl.eclipse.perspective.actions;

import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.ui.text.Editor;
import org.eclipse.jface.action.IAction;

public class EditValueAction extends AbstractIValueAction {
  @Override
  public void run(IAction action) {
    IValue v = getValue();
    
    if (v != null) {
      Editor.edit(getValue(), true, 2);
    }
  }
}
