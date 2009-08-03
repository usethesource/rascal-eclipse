package org.meta_environment.rascal.eclipse.console.internal;

public interface IInterpreter{
	boolean execute(String command) throws CommandExecutionException;
	void setConsole(InterpreterConsole console);
	String getOutput();
}
