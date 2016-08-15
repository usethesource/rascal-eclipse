package org.rascalmpl.eclipse.nature;

import org.rascalmpl.value.ISourceLocation;

public interface IWarningHandler {
  public void warning(String message, ISourceLocation location);
}
