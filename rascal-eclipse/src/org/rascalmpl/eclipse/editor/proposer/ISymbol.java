package org.rascalmpl.eclipse.editor.proposer;

import org.rascalmpl.value.ISourceLocation;

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
