package org.meta_environment.rascal.eclipse.console.internal;

public interface IInterpreter{
	void initialize();
	boolean execute(String command) throws CommandExecutionException, TerminationException;
	void setConsole(IInterpreterConsole console);
	String getOutput();
	void terminate();
	void storeHistory(CommandHistory history);
}
