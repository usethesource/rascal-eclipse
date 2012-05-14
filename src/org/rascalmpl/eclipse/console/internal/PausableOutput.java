package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;

public interface PausableOutput extends Pausable {
	void output(byte[] b) throws IOException;
}
