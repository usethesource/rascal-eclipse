package org.meta_environment.rascal.eclipse.console.internal;

public interface IInterpreter{
	boolean execute(String command) throws CommandExecutionException;
	String getOutput();
}
