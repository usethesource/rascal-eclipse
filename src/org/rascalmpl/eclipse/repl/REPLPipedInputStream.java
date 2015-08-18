package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class REPLPipedInputStream extends InputStream {
  
  private final ConcurrentLinkedQueue<Byte> queue;
  private final AtomicBoolean closed;

  public REPLPipedInputStream() {
    this.queue = new ConcurrentLinkedQueue<Byte>();
    this.closed = new AtomicBoolean(false);
  }
  
  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }
  
  @Override
  public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
		    throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0) ||
			   ((off + len) > b.length) || ((off + len) < 0)) {
		    throw new IndexOutOfBoundsException();
		} else if (len == 0) {
		    return 0;
		}
		// we have to at least read one (so block until we can)
		int atLeastOne = read();
		if (atLeastOne == -1) {
		  return -1;
		}
		int index = off;
		b[index++] = (byte) atLeastOne;

		// now consume the rest of the available bytes
		Byte current;
		while ((current = queue.poll()) != null) {
		  b[index++] = current;
		}
		return index - off;
  }

  @Override
  public int read() throws IOException {
    Byte result = null;
    while ((result = queue.poll()) == null) {
     if (closed.get()) {
       return -1;
     }
     try {
       Thread.sleep(10);
     }
     catch (InterruptedException e) {
       return -1;
     }
    }
    return (result & 0xFF);
  }

  @Override
  public void close() throws IOException {
    closed.set(true);
  }
  
  public void write(byte[] b, int off, int len) {
    for (int i = off; i < off + len; i++) {
     queue.add(b[i]); 
    }
  }
  public void write(byte b) {
     queue.add(b); 
  }

}
