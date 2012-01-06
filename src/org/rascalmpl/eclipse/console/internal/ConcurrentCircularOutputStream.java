package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * This OutputStream allows multiple threads to write to it without requiring any locking.
 * The retrieving (getBufferCopy) of the data in the stream however locks internally and you should avoid calling it
 * from other threads.
 * 
 * To avoid possible deadlocks we advise to  call getBufferCopy in a separate worker (outside of the UI-thread).
 * 
 * @author Davy Landman 
 *
 */
public class ConcurrentCircularOutputStream extends OutputStream {
	private final byte[] buffer;
	private final int bufferSize;
	private final int bufferMod;
	private final AtomicInteger bytesWritten;
	private final ReadWriteLock resultRWLock; // we are actually using it reverse, multiple writers single reader..
	private boolean isDirty; 
	
	
	private final IBufferFlushNotifier notifier;

	/**
	 * Create a new ConcurrentCircularOutputStream which allows for fast writing from multiple threads.
	 * Correct order and interleaving between write()'s are guaranteed.
	 * @param bufferSize the size of the circular buffer, due to implementation details this size must be a power of 2!
	 */
	public ConcurrentCircularOutputStream(int bufferSize, IBufferFlushNotifier notifier) {
		assert bufferSize > 0;
		if ((bufferSize & (bufferSize - 1)) != 0) {
			throw new IllegalArgumentException("bufferSize must be a power of 2");
		}
		this.bufferSize = bufferSize;
		this.bufferMod = bufferSize - 1;
		buffer = new byte[this.bufferSize];
		bytesWritten = new AtomicInteger(0);
		resultRWLock = new ReentrantReadWriteLock(true);
		isDirty = false;
		
		this.notifier = notifier;
	}
	
	@Override
	public void write(int b) throws IOException {
		resultRWLock.readLock().lock();
		try {
			isDirty = true;
			int endPos = bytesWritten.incrementAndGet();
			int writePos =(endPos - 1) & bufferMod;
			buffer[writePos] = (byte)b;
		}
		finally {
			resultRWLock.readLock().unlock();
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		// check params
		if (b == null) {
		    throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0) ||
			   ((off + len) > b.length) || ((off + len) < 0)) {
		    throw new IndexOutOfBoundsException();
		} else if (len == 0) {
		    return;
		}
		if (len > bufferSize) {
			// we can skip until the last part
			off = off + (len - bufferSize);
			len = bufferSize;
		}
		
		resultRWLock.readLock().lock();
		try {
			isDirty = true;
			int endPos = bytesWritten.addAndGet(len);
			int startPos = (endPos - len) & bufferMod;
			
			if (startPos + len <= bufferSize) {
				// go ahead and write!
				System.arraycopy(b, off, buffer, startPos, len);
			} 
			else {
				// we have to write over the edge of the buffer. so be smarter here!
				int firstChunkLength = bufferSize - startPos;
				System.arraycopy(b, off, buffer, startPos, firstChunkLength);
				// now we have to go back to the front of the buffer
				int secondChunkLength = len - firstChunkLength;
				System.arraycopy(b, off + firstChunkLength, buffer, 0, secondChunkLength);
			}
		}
		finally {
			resultRWLock.readLock().unlock();
		}
	}
	
	public boolean getIsDirty() {
		return isDirty;
	}

	/**
	 * This method creates a copy of what has been written in the buffer since the last call to this method.
	 * If this was more than the buffer size, it is off course truncated to that limit.
	 * This call waits for all the writes to finish and than blocks the writes until it is finished, therefore 
	 * be careful with the calling frequency.
	 * 
	 * @return a copy of the bytes in the buffer.
	 */
	public byte[] getBufferCopy() {
		resultRWLock.writeLock().lock();
		try {
			int totalWritten = bytesWritten.get();
			byte[] result;
			if (totalWritten > 0 && totalWritten < bufferSize) {
				// not run outside of the first buffer yet
				result = new byte[totalWritten];
				System.arraycopy(buffer, 0, result, 0, totalWritten);
			}
			else if (isDirty) {
				// pos could be 0 but that could be either that no write has happend yet
				// or that we just returned to 0 after a lot of writes
				result = new byte[bufferSize];
				int bufferOffset = totalWritten & bufferMod;
				int firstChunkLength = bufferOffset;// first chunk is the last part of the array!
				int secondChunkLength = bufferSize - firstChunkLength;
				System.arraycopy(buffer, bufferSize - secondChunkLength, result, 0, secondChunkLength);
				System.arraycopy(buffer, 0, result, secondChunkLength, firstChunkLength);
			}
			else {
				result = new byte[0];
			}
			bytesWritten.set(0);
			isDirty = false;
			return result;
		}
		finally {
			resultRWLock.writeLock().unlock();
		}
	}
	
	@Override
	public void flush() throws IOException {
		int bufferPosition = bytesWritten.get();
		
		if (bufferPosition < 0 || (bufferPosition > bufferSize / 2)) {
			notifier.signalFlush();
		}
	}
}
