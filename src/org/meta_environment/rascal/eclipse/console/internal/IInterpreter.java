package org.meta_environment.rascal.eclipse.console.internal;

import java.io.OutputStream;

public interface IInterpreter{
	boolean execute(String command) throws CommandExecutionException;
	void setOutputStream(OutputStream outputStream);
	String getOutput();
}
