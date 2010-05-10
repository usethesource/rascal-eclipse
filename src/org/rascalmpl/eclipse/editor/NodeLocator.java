package org.rascalmpl.eclipse.editor;

import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.rascalmpl.ast.AbstractAST;
import org.rascalmpl.eclipse.outline.TreeModelBuilder.Group;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.ParsetreeAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;

public class NodeLocator implements ISourcePositionLocator {

	public Object findNode(Object ast, int offset) {
		if (ast instanceof IConstructor) {
			IConstructor cons = (IConstructor) ast;
			if (cons.getConstructorType() == Factory.ParseTree_Top) {
				return findNode(ParsetreeAdapter.getTop(cons), offset);
			}
			else if (cons.getType() == Factory.Tree) {
				return TreeAdapter.locateLexical((IConstructor) ast, offset);
			}
		}
		else if (ast instanceof AbstractAST) {
			return findNode(((AbstractAST) ast).getTree(), offset);
		}
		else if (ast instanceof ModelTreeNode) {
			return findNode(((ModelTreeNode) ast).getASTNode(), offset);
		}
		return null;
	}

	public Object findNode(Object ast, int startOffset, int endOffset) {
		if (ast instanceof IConstructor) {
			IConstructor cons = (IConstructor) ast;
			if (cons.getConstructorType() == Factory.ParseTree_Top) {
				return findNode(ParsetreeAdapter.getTop(cons), startOffset, endOffset);
			}
			else if (cons.getType() == Factory.Tree) {
				return TreeAdapter.locateLexical((IConstructor) ast, startOffset);
			}
		}
		else if (ast instanceof AbstractAST) {
			return findNode(((AbstractAST) ast).getTree(), startOffset);
		}
		else if (ast instanceof ModelTreeNode) {
			return findNode(((ModelTreeNode) ast).getASTNode(), startOffset);
		}
		
		return null;
	}

	public int getEndOffset(Object node) {
		return getStartOffset(node) + getLength(node) - 1;
	}

	public int getLength(Object node) {
		return getLocation(node) == null ? 0 : getLocation(node).getLength();
	}

	public IPath getPath(Object node) {
		return null;
	}

	public int getStartOffset(Object node) {
		return getLocation(node) == null ? 0 : getLocation(node).getOffset();
	}
	
	private ISourceLocation getLocation(Object node) {
		if (node instanceof Token) {
			return ((Token) node).getLocation();
		}
		else if (node instanceof IConstructor) {
			return TreeAdapter.getLocation((IConstructor) node);
		}
		else if (node instanceof AbstractAST) {
			return getLocation(((AbstractAST) node).getTree());
		}
		else if (node instanceof ModelTreeNode) {
			return getLocation(((ModelTreeNode) node).getASTNode());
		}
		else if (node instanceof Group<?>) {
			Group<?> group = (Group<?>) node;
			Iterator<?> i = group.iterator();
			if (i.hasNext()) {
				return getLocation(i.next());
			}
			return group.getLocation();
		}
		throw new RuntimeException("Unknown node type " + node);
	}
}
