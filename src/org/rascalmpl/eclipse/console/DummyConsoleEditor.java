package org.rascalmpl.eclipse.console;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

public class DummyConsoleEditor extends EditorPart {

  public DummyConsoleEditor() {  }

  @Override
  public void doSave(IProgressMonitor monitor) {   }

  @Override
  public void doSaveAs() {  }

  @Override
  public void init(IEditorSite site, IEditorInput input) throws PartInitException {  }

  @Override
  public boolean isDirty() {
    return false;
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  @Override
  public void createPartControl(Composite parent) {
    // do nothing because we do not want to show anything...
    // BTW, this editor is for showing nothing on the bottom Rascal stackframe which
    // is initiated on the commandline prompt.
  }

  @Override
  public void setFocus() {
  }
}
