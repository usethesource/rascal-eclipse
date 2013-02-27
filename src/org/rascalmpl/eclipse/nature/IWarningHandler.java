package org.rascalmpl.eclipse.nature;

import org.eclipse.imp.pdb.facts.ISourceLocation;

public interface IWarningHandler {
  public void warning(String message, ISourceLocation location);
}
