package org.rascalmpl.eclipse.console.internal;

public class CommandExecutionException extends Exception{
	private static final long serialVersionUID = -7113571508827328424L;
	
	private final int offset;
	private final int length;
	
	public CommandExecutionException(String message){
		super(message);
		
		this.offset = -1;
		this.length = -1;
	}
	
	public CommandExecutionException(String message, int offset, int length){
		super(message);
		
		this.offset = offset;
		this.length = length;
	}
	
	public int getOffset(){
		return offset;
	}
	
	public int getLength(){
		return length;
	}
}
