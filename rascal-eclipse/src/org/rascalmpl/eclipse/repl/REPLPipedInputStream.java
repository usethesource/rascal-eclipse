package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class REPLPipedInputStream extends InputStream {

  private final ConcurrentLinkedQueue<Byte> queue;
  private volatile boolean closed;
  private final Semaphore newData = new Semaphore(0);

  public REPLPipedInputStream() {
    this.queue = new ConcurrentLinkedQueue<Byte>();
    this.closed = false;
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
    while ((current = queue.poll()) != null && (index < off + len)) {
      b[index++] = current;
    }
    return index - off;
  }

  @Override
  public int read() throws IOException {
    Byte result = null;
    while ((result = queue.poll()) == null) {
      try {
        newData.tryAcquire(10, TimeUnit.MILLISECONDS);
      }
      catch (InterruptedException e) {
        return -1;
      }
      if (closed) {
        return -1;
      }
    }
    return (result & 0xFF);
  }

  @Override
  public void close() throws IOException {
    closed = true;
  }

  public void write(byte[] b, int off, int len) {
    for (int i = off; i < off + len; i++) {
      queue.add(b[i]); 
    }
    newData.release();
  }
  public void write(byte b) {
    queue.add(b); 
    newData.release();
  }

}
