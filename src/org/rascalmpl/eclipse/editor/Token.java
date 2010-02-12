package org.rascalmpl.eclipse.editor;

import org.eclipse.imp.pdb.facts.ISourceLocation;


public class Token {
	private final String category;
	private final ISourceLocation loc;
	
	public Token(String category, ISourceLocation area) {
		this.category = category;
		this.loc = area;
	}
	
	public String getCategory() {
		return category;
	}
	
	public ISourceLocation getLocation() {
		return loc;
	}
}
