package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class REPLPipedInputStream extends InputStream {

  private final BlockingQueue<Byte> queue;
  private volatile boolean closed;

  public REPLPipedInputStream() {
    this.queue = new ArrayBlockingQueue<>(8 * 1024);
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
    try {
    	while ((result = queue.poll(10, TimeUnit.MILLISECONDS)) == null) {
    		if (closed) {
    			return -1;
    		}
    	}
    }
    catch (InterruptedException e) {
    	return -1;
    }
    return (result & 0xFF);
  }

  @Override
  public void close() {
    closed = true;
  }
  
  public void write(byte[] b) throws IOException {
	  write(b, 0, b.length);
  }

  public void write(byte[] b, int off, int len) throws IOException {
	  try {
		  for (int i = off; i < off + len; i++) {
			  queue.put(b[i]);
		  }
	  } catch (InterruptedException e) {
		  throw new IOException(e);
	  } 
  }
  public void write(byte b) throws IOException {
	  try {
		  queue.put(b); 
	  } catch (InterruptedException e) {
		  throw new IOException(e);
	  } 
  }


}
