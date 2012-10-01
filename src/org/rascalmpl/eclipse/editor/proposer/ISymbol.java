package org.rascalmpl.eclipse.editor.proposer;

import org.eclipse.imp.pdb.facts.ISourceLocation;

public interface ISymbol {
	public <T> T accept(SymbolVisitor<T> visitor);
	public String getLabel();
	public ISourceLocation getLocation();
	
	public String getName();
	public String getType();
	public void setLabel(String label);
	
	public String getAttribute(String key);
	public void setAttribute(String key, String value);
}
