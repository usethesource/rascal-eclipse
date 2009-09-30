package org.meta_environment.rascal.eclipse.console.internal;

public class CommandExecutionException extends Exception{
	private static final long serialVersionUID = -4226914269976638377L;
	
	private final int offset;
	
	public CommandExecutionException(String message, int offset){
		super(message);
		
		this.offset = offset;
	}
	
	public int getOffset(){
		return offset;
	}
}
