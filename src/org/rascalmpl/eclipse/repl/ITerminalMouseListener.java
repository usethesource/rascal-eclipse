package org.rascalmpl.eclipse.repl;

import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;

public interface ITerminalMouseListener {
  void mouseDoubleClick(ITerminalTextDataReadOnly terminalText, int line, int offset);
  void mouseDown(ITerminalTextDataReadOnly terminalText, int line, int offset);
  void mouseUp(ITerminalTextDataReadOnly terminalText, int line, int offset);
}
