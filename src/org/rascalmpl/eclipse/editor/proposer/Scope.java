package org.rascalmpl.eclipse.editor.proposer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.imp.pdb.facts.ISourceLocation;

public class Scope implements ISymbol {
	private final ISymbol scopeSymbol;
	private final List<ISymbol> symbols;
	private final ISourceLocation location;
	private String label;

	public Scope(ISymbol scopeSymbol) {
		if (scopeSymbol == null) {
			throw new IllegalArgumentException("scopeSymbol");
		}

		this.scopeSymbol = scopeSymbol;
		this.location = scopeSymbol.getLocation();
		label = scopeSymbol.getName();
		symbols = new ArrayList<ISymbol>();
	}

	public Scope(ISymbol scopeSymbol, ISourceLocation location) {
		this.scopeSymbol = scopeSymbol;
		this.label = scopeSymbol != null ? scopeSymbol.getName() : "";
		this.location = location;
		symbols = new ArrayList<ISymbol>();
	}

	@Override
	public <T> T accept(SymbolVisitor<T> visitor) {
		return visitor.visitScope(this);
	}

	public void addSymbol(ISymbol symbol) {
		if (symbol != null) {
			symbols.add(symbol);
		}
	}

	public void addSymbols(java.util.List<ISymbol> symbols) {
		if (symbols != null) {
			this.symbols.addAll(symbols);
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
		return symbols;
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
		return scopeSymbol != null ? "Scope@" + scopeSymbol.getType() + "." + scopeSymbol.getName() : "Scope@?";
	}

	@Override
	public String getAttribute(String key) {
		return null;
	}

	@Override
	public void setAttribute(String key, String value)  {}
}
