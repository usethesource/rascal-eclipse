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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.jface.text.IRegion;
import org.rascalmpl.values.uptr.ProductionAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;
import org.rascalmpl.values.uptr.visitors.TreeVisitor;

public class TokenIterator implements Iterator<Token>{
	private final List<Token> tokenList;
	private final Iterator<Token> tokenIterator;
	private boolean showAmb;
	private boolean[] inRegion;
	private boolean hasRegions = false;
	
	public TokenIterator(boolean showAmb, IConstructor parseTree){
		this.tokenList = new ArrayList<Token>(1000);
		this.showAmb = false;
		
		if(parseTree != null){
			ISourceLocation loc = TreeAdapter.getLocation(parseTree);
			if(EditableRegionsRegistry.hasRegistryForDocument(loc)){
				this.hasRegions  = true;
				Collection<IRegion> regions = EditableRegionsRegistry.getRegistryForDocument(loc).values();
				IRegion[] regionsArray = regions.toArray(new IRegion[0]);
				if (regionsArray.length > 0) {
					IRegion lastRegion = regionsArray[regionsArray.length - 1];
					inRegion = new boolean[lastRegion.getOffset()+lastRegion.getLength()+1];
				}
				else {
					inRegion = new boolean[0];
				}
				for (IRegion region:regions){
					for (int i=region.getOffset(); i<=region.getOffset()+region.getLength(); i++)
						inRegion[i] = true;
			}
		  }
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

	private boolean inRegion(int offset){
		if (inRegion !=null){
			if (offset<inRegion.length){
				return inRegion[offset];
			}
		}
		return false;
	}
	
	private class LexicalCollector extends TreeVisitor<RuntimeException>{
		private int location;
		
		public LexicalCollector(){
			super();
			
			location = 0;
		}
		
		private Token getToken(String category, int start, int length){
			if (category != null) {
				if (hasRegions){
					if (inRegion(start))
						return new Token(TokenColorer.REGION, start, length);
				}
				return new Token(category, start, length);
			}else{
				if (hasRegions)
					if (inRegion(start))
						return new Token(TokenColorer.REGION, start, length);
				return null;
			}
		}
		
		public IConstructor visitTreeAmb(IConstructor arg) {
			if (showAmb) {
				int offset = location;
				ISourceLocation ambLoc = TreeAdapter.getLocation(arg);
				int length = ambLoc != null ? ambLoc.getLength() : TreeAdapter.yield(arg).length();

				location += length;
				tokenList.add(new Token(TokenColorer.META_AMBIGUITY, offset, length));
			}
			else {
				TreeAdapter.getAlternatives(arg).iterator().next().accept(this);
			}
			return arg;
			
		}
		
		public IConstructor visitTreeAppl(IConstructor arg){
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
			if(TokenColorer.NONTERMINAL_LABEL.equals(ProductionAdapter.getSortName(prod))){
				category = TokenColorer.NONTERMINAL_LABEL;
			}
			
			// short cut, if we have source locations and a category we found a long token
			ISourceLocation loc = TreeAdapter.getLocation(arg);
			if (loc != null){
				Token t=getToken(category, location, loc.getLength());
				if (t!=null)
					tokenList.add(t);
			}
			
			// now we go down in the tree to find more tokens
			int offset = location;
			
			for (IValue child : TreeAdapter.getArgs(arg)){
				child.accept(this);
			}

			if (ProductionAdapter.isSkipped(prod)) {
				category = TokenColorer.META_SKIPPED;
			}
			
			if (ProductionAdapter.isDefault(prod) && (TreeAdapter.isLiteral(arg) || TreeAdapter.isCILiteral(arg))) {
				if (category == null){
					category = TokenColorer.META_KEYWORD;

					for (IValue child : TreeAdapter.getArgs(arg)) {
						int c = TreeAdapter.getCharacter((IConstructor) child);
						if (c != '-' && !Character.isJavaIdentifierPart(c)){
							category = null;
						}
					}
					
					if (category == null) {
						category = TokenColorer.NORMAL;
					}
				}
			}
			
			Token t=getToken(category, offset, location - offset);
			if (t!=null)
				tokenList.add(t);
			return arg;
		}
		
		public IConstructor visitTreeChar(IConstructor arg){
			++location;
			
			return arg;
		}
		
		public IConstructor visitTreeCycle(IConstructor arg){
			return arg;
		}
	}
}
