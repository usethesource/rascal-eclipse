package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;

public class PausableOutputBuffer implements PausableOutput {

	private final OutputStream stream;
	private final Pausable pausableTarget;

	PausableOutputBuffer(OutputStream stream, Pausable pausableTarget){
		this.stream = stream;
		this.pausableTarget = pausableTarget;
	}
	
	@Override
	public void output(byte[] b) throws IOException {
		stream.write(b);
		stream.flush();
	}

	@Override
	public boolean isPaused() {
		return pausableTarget.isPaused();
	}

}
