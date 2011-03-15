package org.rascalmpl.eclipse.editor;

public class Token{
	private final String category;
	
	private final int offset;
	private final int length;
	
	public Token(String category, int offset, int length){
		super();
		
		this.category = category;
		this.offset = offset;
		this.length = length;
	}
	
	public String getCategory(){
		return category;
	}
	
	public int getOffset(){
		return offset;
	}
	
	public int getLength(){
		return length;
	}
}
