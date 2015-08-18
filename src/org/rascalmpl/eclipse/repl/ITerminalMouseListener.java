package org.rascalmpl.eclipse.repl;

public interface ITerminalMouseListener {
  void mouseDoubleClick(String line, int offset);
  void mouseDown(String line, int offset);
  void mouseUp(String line, int offset);
}
