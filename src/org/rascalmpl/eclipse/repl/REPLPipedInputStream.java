package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;

/**
 * Only works if there is just one thread reading, and one thread writing
 */
public class REPLPipedInputStream extends InputStream {

  private static final int BUFFER_SIZE = 2 << 20;
  private static final long BUFFER_SIZE_MOD = BUFFER_SIZE - 1;
  private final byte[] buffer = new byte[BUFFER_SIZE]; // 1 MB buffer
  private volatile long written = 0;
  private volatile long read = 0;
  private final Semaphore needData = new Semaphore(0);
  private final Semaphore needSpace = new Semaphore(0);
  

  @Override
  public int read() throws IOException {
    long consumed = read;
    long available = written;
    while (consumed == available) {
      try {
        needData.acquire();
      }
      catch (InterruptedException e) {
        throw new IOException(e);
      }
      available = written;
    }
    int readIndex = (int)(consumed & BUFFER_SIZE_MOD);
    int result = buffer[readIndex] & 0xFF;
    read++;
    needData.drainPermits();
    needSpace.release();
    return result;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    assert len < BUFFER_SIZE;
		if (b == null) {
		    throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0) ||
			   ((off + len) > b.length) || ((off + len) < 0)) {
		    throw new IndexOutOfBoundsException();
		} else if (len == 0) {
		    return 0;
		}

    long startOffset = read;
    long available = written;
    while (startOffset == available) {
      try {
        needData.acquire();
      }
      catch (InterruptedException e) {
        throw new IOException(e);
      }
      available = written;
    }
    int availableLength = (int) (available - startOffset);
    // now the data is available
    int startIndex = (int) (startOffset & BUFFER_SIZE_MOD);
    if (startIndex + availableLength < BUFFER_SIZE) {
      // normal subcopy
      System.arraycopy(buffer, startIndex, b, off, availableLength);
    }
    else {
      int firstChunkLength = BUFFER_SIZE - startIndex;
      System.arraycopy(buffer, startIndex, b, off, firstChunkLength);
      System.arraycopy(buffer, 0, b, off + firstChunkLength, availableLength - firstChunkLength);
    }
    read += availableLength;
    needData.drainPermits();
    needSpace.release();
    return availableLength;
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }
  
	public void write(int b) throws IOException {
	  long consumed = written;
	  long available = read + BUFFER_SIZE;
	  while (available >= consumed) {
      try {
        needSpace.acquire();
      }
      catch (InterruptedException e) {
        throw new IOException(e);
      }
	    available = read + BUFFER_SIZE;
	  }
    int writeIndex = (int)(consumed & BUFFER_SIZE_MOD);
    buffer[writeIndex] = (byte)b;
    written++;
    needSpace.drainPermits();
    needData.release();
	}
	public void write(byte[] b, int off, int len) throws IOException {
		if (b == null) {
		    throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0) ||
			   ((off + len) > b.length) || ((off + len) < 0)) {
		    throw new IndexOutOfBoundsException();
		} else if (len == 0) {
		    return;
		}
		if (len > BUFFER_SIZE) {
			// we can skip until the last part
			off = off + (len - BUFFER_SIZE);
			len = BUFFER_SIZE;
		}
		
		
	  long consumed = written + len;
	  long available = read + BUFFER_SIZE;
	  while (available >= consumed) {
      try {
        needSpace.acquire();
      }
      catch (InterruptedException e) {
        throw new IOException(e);
      }
	    available = read + BUFFER_SIZE;
	  }
	  
	  int startIndex = (int) ((consumed - len) & BUFFER_SIZE_MOD);
	  if (startIndex + len < BUFFER_SIZE) {
	    System.arraycopy(b, off, buffer, startIndex, len);
	  }
	  else {
	    int firstChunkLength = BUFFER_SIZE - startIndex;
	    System.arraycopy(b, off, buffer, startIndex, firstChunkLength);
	    System.arraycopy(b, off + firstChunkLength, buffer, 0, len - firstChunkLength);
	  }
	  written += len;
    needSpace.drainPermits();
    needData.release();
	}
}
