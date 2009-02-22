package org.meta_environment.rascal.eclipse.editor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.meta_environment.uptr.TreeAdapter;
import org.meta_environment.uptr.visitors.TreeVisitor;

public class TokenIterator implements Iterator<Token> {
	private List<Token> tokenList;
	private Iterator<Token> tokenIterator;

	public TokenIterator(IConstructor parseTree) {
		this.tokenList = new LinkedList<Token>();
		try {
			parseTree.get("top").accept(new LexicalCollector());
		} catch (VisitorException e) {
			// is not thrown
		}
		tokenIterator = tokenList.iterator();
	}

	@Override
	public boolean hasNext() {
		return tokenIterator.hasNext();
	}

	@Override
	public Token next() {
		return tokenIterator.next();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	private class LexicalCollector extends TreeVisitor {
		@Override
		public IConstructor visitTreeAmb(IConstructor arg)
				throws VisitorException {
			tokenList.add(new Token("MetaAmbiguity", new TreeAdapter(arg).getRange()));
			return arg;
		}

		@Override
		public IConstructor visitTreeAppl(IConstructor arg)
				throws VisitorException {
			TreeAdapter tree = new TreeAdapter(arg);
			
			if (tree.isLiteral()) {
				String yield = new TreeAdapter(arg).yield();
				for (byte c : yield.getBytes()) {
					if (!Character.isJavaIdentifierPart(c)) {
						return arg;
					}
				}
				tokenList.add(new Token("MetaKeyword", new TreeAdapter(arg).getRange()));
			}
			else {
				for (IValue child : tree.getArgs()) {
					child.accept(this);
				}
			}

			return arg;
		}

		@Override
		public IConstructor visitTreeChar(IConstructor arg)
				throws VisitorException {
			return arg;
		}

		@Override
		public IConstructor visitTreeCycle(IConstructor arg)
				throws VisitorException {
			return arg;
		}
	}
}
