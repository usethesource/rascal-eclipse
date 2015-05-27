package org.rascalmpl.eclipse.editor.proposer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.imp.editor.ErrorProposal;
import org.eclipse.imp.editor.SourceProposal;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;
import org.rascalmpl.ast.Module;
import org.rascalmpl.parser.ASTBuilder;
import org.rascalmpl.values.uptr.ITree;

/**
 * Content proposer for Rascal.
 */
public class ContentProposer implements IContentProposer {
	private class ProposalComposer extends SymbolVisitor<Boolean> {
		private final List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		private Prefix prefix;

		public List<ICompletionProposal> compose(ISymbol symbolTree, Prefix prefix) {
			if (symbolTree != null) {
				this.prefix = prefix;

				symbolTree.accept(this);
			}

			return proposals;
		}

		@Override
		public Boolean visitScope(Scope scope) {
			ISymbol scopeSymbol = scope.getScopeSymbol();
			if (scopeSymbol != null) {
				scopeSymbol.accept(this);
			}

			for (ISymbol childSymbol : scope.getSymbols()) {
				childSymbol.accept(this);
			}

			return true;
		}

		@Override
		public Boolean visitSymbol(Symbol symbol) {
			String type = symbol.getType();
			String name = symbol.getName();
			String label = symbol.getLabel();

			if (name.toLowerCase().contains(prefix.getText().toLowerCase())) {
				if (type.equals(Symbol.symbol_type_function) || type.equals(Symbol.symbol_type_constructor)) {
					proposals.add(new SourceProposal(label, name + "()", prefix.getText(), prefix.getOffset()));
				} else if (!type.equals(Symbol.symbol_type_module)) {
					proposals.add(new SourceProposal(label, name, prefix.getText(), prefix.getOffset()));
				}
			}

			return true;
		}
	}

	private class ScopeFilter extends SymbolVisitor<ISymbol> {
		private int offset = 0;

		public ISymbol filterTree(ISymbol tree, int offset) {
			if (tree != null && offset >= 0) {
				this.offset = offset;
				return tree.accept(this);
			}

			return tree;
		}

		private boolean isOutsideModuleDeclaration(Scope scope) {
			ISymbol scopeSymbol = scope.getScopeSymbol();
			ISourceLocation scopeLocation = scope.getLocation();
			if (scopeSymbol != null 
					&& scopeSymbol.getType() == Symbol.symbol_type_module 
					&& scopeLocation != null
					&& (scopeLocation.getOffset() + scopeLocation.getLength()) < offset) {
				return true;
			}
			return false;
		}

		private boolean isWithin(ISymbol tree) {
			if (tree == null || tree.getLocation() == null || offset < 0) {
				return false;
			}

			int begin = tree.getLocation().getOffset();
			int end = begin + tree.getLocation().getLength();

			return begin <= offset && end > offset;
		}

		@Override
		public ISymbol visitScope(Scope scope) {
			if (isOutsideModuleDeclaration(scope)) {
				// In case of parse errors and the request is "outside of the file",
				// return everything instead of nothing for convinience.
				return scope;
			}
			
			if (isWithin(scope)) {
				Scope filteredScope = new Scope(scope.getScopeSymbol(), scope.getLocation());

				for (ISymbol childSymbol : scope.getSymbols()) {
					filteredScope.addSymbol(childSymbol.accept(this));
				}

				return filteredScope;
			} else if (scope.getScopeSymbol() != null) {
				return scope.getScopeSymbol();
			}

			return null;
		}

		@Override
		public ISymbol visitSymbol(Symbol symbol) {
			return symbol;
		}

	}

	private class SymbolLabeler extends SymbolVisitor<ISymbol> {
		List<ISymbol> scopeChildren = null;

		public ISymbol generate(ISymbol symbolTree) {
			return symbolTree.accept(this);
		}

		private String getArgumentLabel() {
			String argumentList = "";
			if (scopeChildren != null) {
				for (ISymbol scopeChild : scopeChildren) {
					if (scopeChild.getType().equals(Symbol.symbol_type_arg)) {
						if (!argumentList.isEmpty()) {
							argumentList += ", ";
						}

						argumentList += scopeChild.getAttribute(Symbol.symbol_attribute_datatype) + " " + scopeChild.getName();
					}
				}
			}
			return argumentList;
		}

		@Override
		public ISymbol visitScope(Scope scope) {
			scopeChildren = scope.getSymbols();
			if (scope.getScopeSymbol() != null) {
				scope.getScopeSymbol().accept(this);
			}

			scopeChildren = null;
			for (ISymbol childSymbol : scope.getSymbols()) {
				childSymbol.accept(this);
			}

			return scope;
		}

		@Override
		public ISymbol visitSymbol(Symbol symbol) {
			String type = symbol.getType();
			String name = symbol.getName();
			String datatype = symbol.getAttribute(Symbol.symbol_attribute_datatype);
			if (datatype == null) datatype = Symbol.symbol_datatype_unknown;

			if (type.equals(Symbol.symbol_type_function) || type.equals(Symbol.symbol_type_constructor)) {
				String argumentList = getArgumentLabel();
				symbol.setLabel(name + "(" + argumentList + ") - " + datatype + " - " + type);
			} else if (type.equals(Symbol.symbol_type_adt)) {
				symbol.setLabel(name + " - " + type);
			} else {
				symbol.setLabel(name + " - " + datatype + " - " + type);
			}

			return symbol;
		}
	}

	ISymbol cachedSymbolTree;

	int previousDocumentHash = 0;

	private final String identifier_allowed_chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\\_-";

	private void createProposals(int offset, List<ICompletionProposal> proposals, Prefix prefix, ISymbol symbolTree, boolean filterScope) {
		ISymbol filteredSymbolTree = symbolTree;		
		if (filterScope) {
			ScopeFilter filter = new ScopeFilter();
			filteredSymbolTree = filter.filterTree(symbolTree, offset);
		}
		
		proposals.addAll(new ProposalComposer().compose(filteredSymbolTree, prefix));
	}

	@Override
	public ICompletionProposal[] getContentProposals(IParseController parseController, int requestOffset, ITextViewer textViewer) {
		int currentDocumentHash = parseController.getDocument().get().hashCode();
		List<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();

		ITree tree = (ITree) parseController.getCurrentAst();
		Point selection = textViewer.getSelectedRange();

		Prefix prefix = Prefix.getPrefix(parseController.getDocument(), selection.x, selection.y, identifier_allowed_chars);
		if (tree != null) {
			ISymbol symbolTree = null;
			if (currentDocumentHash != previousDocumentHash) {
				ASTBuilder builder = new ASTBuilder();
				Module moduleAST = builder.buildModule(tree);
				symbolTree = SymbolTreeCreator.create(moduleAST);
				if (symbolTree != null) {
					SymbolLabeler labeler = new SymbolLabeler();
					symbolTree = labeler.generate(symbolTree);
					cachedSymbolTree = symbolTree;
				}
			} else {
				symbolTree = cachedSymbolTree;
			}

			if (symbolTree != null) {
				createProposals(requestOffset, proposals, prefix, symbolTree, true);
			}
		} else {
			if (cachedSymbolTree != null) {
				createProposals(requestOffset, proposals, prefix, cachedSymbolTree, false);
			} else {
				proposals.add(new ErrorProposal("No proposals available: syntax errors.", requestOffset));
			}
		}

		if (proposals.size() == 0) {
			proposals.add(new ErrorProposal("No proposals available.", requestOffset));
		}

		previousDocumentHash = currentDocumentHash;
		return proposals.toArray(new ICompletionProposal[proposals.size()]);
	}
}
