package org.rascalmpl.eclipse.nature;

import io.usethesource.vallang.ISourceLocation;

public interface IWarningHandler {
  public void warning(String message, ISourceLocation location);
}
