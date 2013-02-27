package org.rascalmpl.eclipse.nature;

import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.eclipse.Activator;

public class WarningsToErrorLog implements IWarningHandler {
  @Override
  public void warning(String message, ISourceLocation location) {
    Activator.log(message + " at " + location, new Exception(message));
  }
}
