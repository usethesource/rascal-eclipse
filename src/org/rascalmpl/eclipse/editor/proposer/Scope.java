package org.rascalmpl.eclipse.editor.proposer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.imp.pdb.facts.ISourceLocation;

public class Scope implements ISymbol {
	private final ISymbol scopeSymbol;
	private final List<ISymbol> childSymbols;
	private final ISourceLocation location;
	private String label;

	public Scope(ISymbol scopeSymbol) {
		if (scopeSymbol == null) {
			throw new IllegalArgumentException("scopeSymbol");
		}

		this.scopeSymbol = scopeSymbol;
		this.location = scopeSymbol.getLocation();
		label = scopeSymbol.getName();
		childSymbols = new ArrayList<ISymbol>();
	}

	public Scope(ISymbol scopeSymbol, ISourceLocation location) {
		this.scopeSymbol = scopeSymbol;
		this.label = scopeSymbol != null ? scopeSymbol.getName() : "";
		this.location = location;
		childSymbols = new ArrayList<ISymbol>();
	}

	@Override
	public <T> T accept(SymbolVisitor<T> visitor) {
		return visitor.visitScope(this);
	}

	public void addSymbol(ISymbol symbol) {
		if (symbol != null) {
			childSymbols.add(symbol);
		}
	}

	public void addSymbols(java.util.List<ISymbol> symbols) {
		if (symbols != null) {
			this.childSymbols.addAll(symbols);
		}
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public ISourceLocation getLocation() {
		return location;
	}

	public String getName() {
		return scopeSymbol != null ? scopeSymbol.getName() : "";
	}

	public ISymbol getScopeSymbol() {
		return scopeSymbol;
	}

	public List<ISymbol> getSymbols() {
		return childSymbols;
	}

	public String getType() {
		return scopeSymbol != null ? scopeSymbol.getType() : Symbol.symbol_type_void;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		//scope(SymbolTree scopeSymbol, list[SymbolTree] children)
		//scope(list[SymbolTree] children)
		
		String childListString = "";			
		
		if (childSymbols.size() > 0) {
			for (ISymbol child : childSymbols) {
				if (!childListString.isEmpty()) childListString += ", ";
				childListString += child.toString();
			}
			childListString = "[" + childListString + "]";
		}
		
		if (childListString.isEmpty()) childListString = "[]";
		
		if (scopeSymbol != null) {
			return String.format("scope(%s, %s)", scopeSymbol.toString(), childListString);
		}
		
		return String.format("scope(%s)", childListString);
	}

	@Override
	public String getAttribute(String key) {
		return null;
	}

	@Override
	public void setAttribute(String key, String value)  {}
}
