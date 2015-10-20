package org.rascalmpl.eclipse.nature;

import java.io.PrintWriter;

import org.rascalmpl.value.ISourceLocation;

public class WarningsToPrintWriter implements IWarningHandler {
  private final PrintWriter writer;
  
  public WarningsToPrintWriter(PrintWriter writer) {
    assert writer != null;
    this.writer = writer;
  }
  
  @Override
  public void warning(String message, ISourceLocation location) {
    writer.println(location + ":" + message);
  }
}
