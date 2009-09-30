package org.meta_environment.rascal.eclipse.console.internal;

public class CommandExecutionException extends Exception{
	private static final long serialVersionUID = -7113571508827328424L;
	
	private final int offset;
	
	public CommandExecutionException(String message){
		super(message);
		
		this.offset = -1;
	}
	
	public CommandExecutionException(String message, int offset){
		super(message);
		
		this.offset = offset;
	}
	
	public int getOffset(){
		return offset;
	}
}
