package org.rascalmpl.eclipse.nature;

import java.io.PrintWriter;

import io.usethesource.vallang.ISourceLocation;

public class WarningsToPrintWriter implements IWarningHandler {
	private final PrintWriter writer;

	public WarningsToPrintWriter(PrintWriter writer) {
		assert writer != null;
		this.writer = writer;
	}

	@Override
	public void warning(String message, ISourceLocation location) {
		writer.println(location + ":" + message);
	}

	@Override
	public void clean() {
		// TODO Auto-generated method stub

	}
}
