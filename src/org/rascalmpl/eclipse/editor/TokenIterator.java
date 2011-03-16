package org.rascalmpl.eclipse.editor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.rascalmpl.values.uptr.ProductionAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;
import org.rascalmpl.values.uptr.visitors.TreeVisitor;

public class TokenIterator implements Iterator<Token>{
	private final List<Token> tokenList;
	private final Iterator<Token> tokenIterator;

	public TokenIterator(IConstructor parseTree){
		this.tokenList = new LinkedList<Token>();
		
		if(parseTree != null){
			try{
				parseTree.accept(new LexicalCollector());
			}catch(VisitorException e){
				// is not thrown
			}
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

	private class LexicalCollector extends TreeVisitor{
		private int location;
		
		public LexicalCollector(){
			super();
			
			location = 0;
		}
		
		public IConstructor visitTreeAmb(IConstructor arg) throws VisitorException{
			// we just go into the first, it's arbitrary but at least we'll get some nice highlighting
			TreeAdapter.getAlternatives(arg).iterator().next().accept(this);
			return arg;
		}
		
		public IConstructor visitTreeAppl(IConstructor arg) throws VisitorException{
			String category = ProductionAdapter.getCategory(TreeAdapter.getProduction(arg));
			
			int offset = location;
			
			if(TreeAdapter.isLiteral(arg)){
				if(category == null){
					String yield = TreeAdapter.yield(arg);
					for(byte c : yield.getBytes()) {
						if(c != '-' && !Character.isJavaIdentifierPart(c)){
							location += TreeAdapter.yield(arg).length();
							return arg;
						}
					}
					category = TokenColorer.META_KEYWORD;
				}
			}
			
			if(category == null){
				for(IValue child : TreeAdapter.getArgs(arg)){
					child.accept(this);
				}
			}else{
				location += TreeAdapter.yield(arg).length();
				
				tokenList.add(new Token(category, offset, location - offset));
			}

			return arg;
		}
		
		public IConstructor visitTreeChar(IConstructor arg) throws VisitorException{
			++location;
			
			return arg;
		}
		
		public IConstructor visitTreeCycle(IConstructor arg) throws VisitorException{
			return arg;
		}
		
		public IConstructor visitTreeError(IConstructor arg) throws VisitorException{
			String category = ProductionAdapter.getCategory(TreeAdapter.getProduction(arg));
			
			int offset = location;
			
			if(TreeAdapter.isLiteral(arg)){
				if(category == null){
					String yield = TreeAdapter.yield(arg);
					for(byte c : yield.getBytes()) {
						if(c != '-' && !Character.isJavaIdentifierPart(c)){
							location += TreeAdapter.yield(arg).length();
							return arg;
						}
					}
					category = TokenColorer.META_KEYWORD;
				}
			}
			
			if(category == null){
				for(IValue child : (IList) arg.get("args")){
					child.accept(this);
				}
			}else{
				location += TreeAdapter.yield(arg).length();
				
				tokenList.add(new Token(category, offset, location - offset));
			}

			return arg;
		}
		
		public IConstructor visitTreeExpected(IConstructor arg) throws VisitorException{
			return arg;
		}
	}
}
