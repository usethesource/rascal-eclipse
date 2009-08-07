package org.meta_environment.rascal.eclipse.console.internal;

import java.io.OutputStream;

public interface IInterpreterConsole{
	OutputStream getConsoleOutputStream();
	void terminate();
	void executeCommand(String command);
	IInterpreter getInterpreter();
}
