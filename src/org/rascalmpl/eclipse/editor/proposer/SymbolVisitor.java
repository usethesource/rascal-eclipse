package org.rascalmpl.eclipse.editor.proposer;

public abstract class SymbolVisitor<T> {
	public abstract T visitScope(Scope scope);
	public abstract T visitSymbol(Symbol symbol);
}
