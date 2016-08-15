package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.OutputStream;

public class REPLPipedOutputStream extends OutputStream {
  
  private final REPLPipedInputStream otherSide;

  public REPLPipedOutputStream(REPLPipedInputStream otherSide) {
    this.otherSide = otherSide;
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
		if (b == null) {
		    throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0) ||
			   ((off + len) > b.length) || ((off + len) < 0)) {
		    throw new IndexOutOfBoundsException();
		} else if (len == 0) {
		    return;
		}
		otherSide.write(b, off, len);
  }

  @Override
  public void write(int b) throws IOException {
    otherSide.write((byte)b);
  }

}
