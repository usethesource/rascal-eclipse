/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.rascalmpl.values.uptr.ProductionAdapter;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.TreeAdapter;
import org.rascalmpl.values.uptr.visitors.TreeVisitor;

public class TokenIterator implements Iterator<Object>{
	private final List<Token> tokenList;
	private final Iterator<Token> tokenIterator;
	private boolean showAmb;

	public TokenIterator(boolean showAmb, IConstructor parseTree){
		this.tokenList = new ArrayList<Token>(1000);
		this.showAmb = false;
		
		if(parseTree != null){
		  parseTree.accept(new LexicalCollector());
		}
		tokenIterator = tokenList.iterator();
	}

	public boolean hasNext(){
		return tokenIterator.hasNext();
	}

	public Token next(){
		return tokenIterator.next();
	}

	public void remove(){
		throw new UnsupportedOperationException();
	}

	private class LexicalCollector extends TreeVisitor<RuntimeException>{
		private int location;
		
		public LexicalCollector(){
			super();
			
			location = 0;
		}
		
		public ITree visitTreeAmb(ITree arg) {
			if (showAmb) {
				int offset = location;
				ISourceLocation ambLoc = TreeAdapter.getLocation(arg);
				int length = ambLoc != null ? ambLoc.getLength() : TreeAdapter.yield(arg).length();

				location += length;
				tokenList.add(new Token(TreeAdapter.META_AMBIGUITY, offset, length));
			}
			else {
				TreeAdapter.getAlternatives(arg).iterator().next().accept(this);
			}
			return arg;
			
		}
		
		public ITree visitTreeAppl(ITree arg){
			IValue catAnno = arg.asAnnotatable().getAnnotation("category");
			String category = null;
			
			if (catAnno != null) {
				category = ((IString) catAnno).getValue();
			}
			
			IConstructor prod = TreeAdapter.getProduction(arg);
			if (category == null && ProductionAdapter.isDefault(prod)) {
				category = ProductionAdapter.getCategory(prod);
			}
			
			// It's not so nice to link the sort name to the token color constant ...
			if(TreeAdapter.NONTERMINAL_LABEL.equals(ProductionAdapter.getSortName(prod))){
				category = TreeAdapter.NONTERMINAL_LABEL;
			}
			
			// short cut, if we have source locations and a category we found a long token
			ISourceLocation loc = TreeAdapter.getLocation(arg);
			
			// Always sync location with locs because of concrete syntax stuff in Rascal.
			if (loc != null) {
				location = loc.getOffset();
			}
			
			
			if (category != null && loc != null) {
				tokenList.add(new Token(category, location, loc.getLength()));
				location += loc.getLength();
				return arg;
			}
			
			
			// now we go down in the tree to find more tokens
			int offset = location;
			
			for (IValue child : TreeAdapter.getArgs(arg)){
				child.accept(this);
			}

			if (ProductionAdapter.isSkipped(prod)) {
				category = TreeAdapter.META_SKIPPED;
			}
			
			if (ProductionAdapter.isDefault(prod) && (TreeAdapter.isLiteral(arg) || TreeAdapter.isCILiteral(arg))) {
				if (category == null){
					category = TreeAdapter.META_KEYWORD;

					for (IValue child : TreeAdapter.getArgs(arg)) {
						int c = TreeAdapter.getCharacter((ITree) child);
						if (c != '-' && !Character.isJavaIdentifierPart(c)){
							category = null;
						}
					}
					
					if (category == null) {
						category = TreeAdapter.NORMAL;
					}
				}
			}
			
			if (category != null) {
				tokenList.add(new Token(category, offset, loc != null ? loc.getLength() : location - offset));
			}

			return arg;
		}
		
		public ITree visitTreeChar(ITree arg){
			++location;
			
			return arg;
		}
		
		public ITree visitTreeCycle(ITree arg){
			return arg;
		}
	}
}
