package org.rascalmpl.eclipse.editor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.rascalmpl.values.uptr.ProductionAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;
import org.rascalmpl.values.uptr.visitors.TreeVisitor;

public class TokenIterator implements Iterator<Token> {
	private final List<Token> tokenList;
	private final Iterator<Token> tokenIterator;

	public TokenIterator(IConstructor parseTree) {
		this.tokenList = new LinkedList<Token>();
		
		if (parseTree != null) {
			try {
				parseTree.get("top").accept(new LexicalCollector());
			} catch (VisitorException e) {
				// is not thrown
			}
		}
		tokenIterator = tokenList.iterator();
	}

	public boolean hasNext() {
		return tokenIterator.hasNext();
	}

	public Token next() {
		return tokenIterator.next();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	private class LexicalCollector extends TreeVisitor {
		@Override
		public IConstructor visitTreeAmb(IConstructor arg) throws VisitorException {
			tokenList.add(new Token(TokenColorer.META_AMBIGUITY, TreeAdapter.getLocation(arg)));
			return arg;
		}

		@Override
		public IConstructor visitTreeAppl(IConstructor arg)
				throws VisitorException {
			String category = TreeAdapter.isAppl(arg) ? ProductionAdapter.getCategory(TreeAdapter.getProduction(arg)) : null ;
			
			if (category == null) {
				if (TreeAdapter.isLiteral(arg)) {
					String yield = TreeAdapter.yield(arg);
					for (byte c : yield.getBytes()) {
						if (!Character.isJavaIdentifierPart(c)) {
							return arg;
						}
					}
					category = TokenColorer.META_KEYWORD;
				}
			}
			
			if (category == null) {
				for (IValue child : TreeAdapter.getArgs(arg)) {
					child.accept(this);
				}
			} else {
				tokenList.add(new Token(category, TreeAdapter.getLocation(arg)));
			}

			return arg;
		}

		@Override
		public IConstructor visitTreeChar(IConstructor arg) throws VisitorException {
			return arg;
		}

		@Override
		public IConstructor visitTreeCycle(IConstructor arg) throws VisitorException {
			return arg;
		}
	}
}
