package org.rascalmpl.eclipse.nature;

import org.rascalmpl.eclipse.Activator;
import io.usethesource.vallang.ISourceLocation;

public class WarningsToErrorLog implements IWarningHandler {
  @Override
  public void warning(String message, ISourceLocation location) {
    Activator.log(message + " at " + location, new Exception(message));
  }
}
