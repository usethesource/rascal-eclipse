package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;


public class ConcurrentCircularOutputStream extends OutputStream {
	private final byte[] buffer;
	private final int bufferSize;
	private final int bufferMod;
	private final AtomicInteger bytesWritten;
	
	public ConcurrentCircularOutputStream(int bufferSize) {
		assert bufferSize > 0;
		if ((bufferSize & (bufferSize - 1)) != 0) {
			throw new IllegalArgumentException("bufferSize must be a power of 2");
		}
		this.bufferSize = bufferSize;
		this.bufferMod = bufferSize - 1;
		buffer = new byte[this.bufferSize];
		bytesWritten = new AtomicInteger(0);
	}
	
	@Override
	public void write(int b) throws IOException {
		int endPos = bytesWritten.incrementAndGet();
		int writePos =(endPos - 1) & bufferMod;
		buffer[writePos] = (byte)b;
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
		writeChecked(b, off, len);
	}
	
	private void writeChecked(byte[] b, int off, int len) {
		assert len <= bufferSize;
		
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
	
	/**
	 * You should only call this from 1 thread!
	 * @return
	 */
	public byte[] catchUpToBuffer() {
		/*
		// we try to get current state 
		int bufferOffset = bufferWritePointer.get();
		int totalBytesWritten = bytesWritten.get();
		// since the bytesWritten and the bufferOffset are changed independently from each other (no double CAS operations)
		// we need to make sure they align
		while (bufferOffset != bufferWritePointer.get() || bufferOffset > bufferSize) {
			bufferOffset = bufferWritePointer.get();
			totalBytesWritten = bytesWritten.get();
			// we are looking for a moment where they remain stable
			// such that we are sure we can read x bytes back from the bufferOffset position
		}
		if (totalBytesWritten == 0) { 
			return new byte[0];
		}
		resetBytesWrittenCounter(totalBytesWritten);
		
		int bytesToCatchUp = Math.min(totalBytesWritten, bufferSize);
		byte[] result = new byte[bytesToCatchUp];
		
		if (bufferOffset - bytesToCatchUp >= 0) {
			//System.err.printf("buffoffset: %d bytecatch: %d\n", bufferOffset, bytesToCatchUp);
			System.arraycopy(buffer, bufferOffset - bytesToCatchUp, result, 0, bytesToCatchUp);
		}
		else {
			// we have to copy twice because we loop around to the end of the buffer
			int firstChunkLength = bufferOffset;// first chunk is the last part of the array!
			int secondChunkLength = bytesToCatchUp - firstChunkLength;
			System.arraycopy(buffer, bufferSize - secondChunkLength, result, 0, secondChunkLength);
			System.arraycopy(buffer, 0, result, secondChunkLength, firstChunkLength);
		}
		return result;
		*/
		return new byte[0];
	}

	private void resetBytesWrittenCounter(int totalBytesWritten) {
		int newByteWrittenCounterValue = 0;
		int oldByteWrittenCounterValue = totalBytesWritten;
		// first let's update the byteWritten counter
		while (!bytesWritten.compareAndSet(oldByteWrittenCounterValue, newByteWrittenCounterValue)) {
			// if we could not update, new bytes were already written
			// so we have to initialize the bytesWritten counter at a different number than 0
			oldByteWrittenCounterValue = bytesWritten.get();
			newByteWrittenCounterValue = Math.max(0, oldByteWrittenCounterValue - totalBytesWritten);
		}
	}
	
}
