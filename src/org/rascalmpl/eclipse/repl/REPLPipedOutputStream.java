package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.OutputStream;

public class REPLPipedOutputStream extends OutputStream {
  

  private final REPLPipedInputStream stdIn;

  public REPLPipedOutputStream(REPLPipedInputStream stdIn) {
    this.stdIn = stdIn;
  }

  @Override
  public void write(int b) throws IOException {
    stdIn.write(b);
  }
  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    stdIn.write(b, off, len);
  }

}
