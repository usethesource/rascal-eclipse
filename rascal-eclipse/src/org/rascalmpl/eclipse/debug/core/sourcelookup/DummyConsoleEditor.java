package org.rascalmpl.eclipse.debug.core.sourcelookup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

/**
 * This class exists to work around the fact that the eclipse debugging ui wants
 * to show an editor for every active stack frame. The stack frame that corresponds to
 * the console repl does not have a source editor. To avoid showing the default 
 * "empty editor" from eclipse, we registered this dummy editor and show actually nothing. 
 * 
 */
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
