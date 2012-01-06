package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;

public class PausableOutputStream extends OutputStream {
	private final OutputWidget outputWidget;

	public PausableOutputStream(OutputWidget outputWidget) {
		// TODO Auto-generated constructor stub
		this.outputWidget = outputWidget;
	}
	@Override
	public void write(int b) throws IOException {
		write(new byte[]{(byte)b});
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		outputWidget.append(new String(b, "UTF8"));
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		outputWidget.append(new String(b, off, len, "UTF8"));
	}
	
	public boolean isPaused() {
		return outputWidget.isPaused();
	}

}
