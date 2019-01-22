/** 
 * Copyright (c) 2019, Davy Landman, SWAT.engineering
 * All rights reserved. 
 *  
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met: 
 *  
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 *  
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */ 
package org.rascalmpl.eclipse.util;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import io.usethesource.impulse.runtime.RuntimePlugin;

public enum ThreadSafeImpulseConsole {
	INSTANCE {
		public final Writer writer = new SyncWriter();

		@Override
		public Writer getWriter() {
			return writer;
		}
	};
	
	public abstract Writer getWriter();
	

	private static final class SyncWriter extends Writer {
		private static final int FLUSH_EVERY_WRITES = 100;

		private final Queue<ByteBuffer> queuedWrites = new ConcurrentLinkedDeque<>();
		private final Semaphore flush = new Semaphore(0, true);
		private final Thread syncWrites;

		public SyncWriter() {
			syncWrites = new Thread(() -> {
				final PrintStream target = RuntimePlugin.getInstance().getConsoleStream();
				while (true) {
					try {
						flush.tryAcquire(FLUSH_EVERY_WRITES, 20, TimeUnit.MILLISECONDS);
						flush.drainPermits(); // avoid multiple flushes
						ByteBuffer toWrite;
						while ((toWrite = queuedWrites.poll()) != null) {
							target.write(toWrite.array(), toWrite.arrayOffset(), toWrite.limit());
						}

					} catch (InterruptedException ie) {
						target.close();
						return;
					}
				}
			});
			syncWrites.setName("Thread Safe Writer to the Impulse Console");
			syncWrites.setDaemon(true);
			syncWrites.start();
		}

		

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			write(CharBuffer.wrap(cbuf, off, len));
		}

		@Override
		public void write(String str, int off, int len) throws IOException {
			write(CharBuffer.wrap(str, off, len));
		}

		@Override
		public void write(int c) throws IOException {
			write(CharBuffer.wrap(new char[] {(char)c }));
		}

		private void write(CharBuffer chars) {
			queuedWrites.add(StandardCharsets.UTF_8.encode(chars));
			flush.release();
		}
		
		@Override
		public void flush() throws IOException {
			flush.release(FLUSH_EVERY_WRITES);
		}

		@Override
		public void close() throws IOException {
			syncWrites.interrupt();
		}
		
	}
}

