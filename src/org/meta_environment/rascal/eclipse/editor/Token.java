package org.meta_environment.rascal.eclipse.editor;

import org.eclipse.imp.pdb.facts.ISourceRange;


public class Token {
	private String category;
	private ISourceRange range;
	
	public Token(String category, ISourceRange area) {
		this.category = category;
		this.range = area;
	}
	
	public String getCategory() {
		return category;
	}
	
	public ISourceRange getRange() {
		return range;
	}
}
