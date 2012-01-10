package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class TimedBufferedPipe implements IBufferFlushNotifier {
	
	public class SyncThread extends Thread {
		private final ConcurrentCircularOutputStream source;
		private final Semaphore flushStream;
		private final PausableOutput target;

		public SyncThread(ConcurrentCircularOutputStream source, PausableOutput target, Semaphore flushStream, String name) {
			super("Console Sync Thread" + name);
			this.source = source;
			this.target = target;
			this.flushStream = flushStream;
		}

		@Override
		public void run() {
			try {
				while (true) {
					// either sleep for 50ms or get a signal to empty the stream earlier
					flushStream.tryAcquire(250L, TimeUnit.MILLISECONDS);
					if (!target.isPaused()) {
						byte[] bufferContents = source.getBufferCopy();
						flushStream.drainPermits(); // reset semaphore
						if (bufferContents.length > 0) {
							try {
								target.output(bufferContents);
							} catch (IOException e) {
								System.err.println("Couldn't send stuff to the actuall console");
							}
						}
					} 
					else {
						flushStream.drainPermits(); // reset semaphore
					}
					
				}
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	private PausableOutput target;
	private Semaphore flushStream;
	private SyncThread syncer;
	private String name;
	
	
	public TimedBufferedPipe(PausableOutput target, String name) {
		this.target = target;
		syncer = null;
		flushStream = new Semaphore(1);
		this.name = name;
	}
	
	public void initializeWithStream(ConcurrentCircularOutputStream source) {
		if (syncer == null) {
			syncer = new SyncThread(source, target, flushStream, name);
			syncer.start();
		}
	}

	@Override
	public void signalFlush() {
		flushStream.release();
	}

}
