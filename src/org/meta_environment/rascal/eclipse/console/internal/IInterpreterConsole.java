package org.meta_environment.rascal.eclipse.console.internal;

import java.io.OutputStream;

import org.eclipse.ui.console.IConsole;

public interface IInterpreterConsole extends IConsole{
	OutputStream getConsoleOutputStream();
	void terminate();
	void executeCommand(String command);
	IInterpreter getInterpreter();
	CommandHistory getHistory(); // NOTE: Optional operation; just return null if you don't have it.
}
