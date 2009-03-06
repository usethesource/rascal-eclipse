package org.meta_environment.rascal.eclipse.editor;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.meta_environment.rascal.ast.AbstractAST;
import org.meta_environment.uptr.TreeAdapter;

public class NodeLocator implements ISourcePositionLocator {

	@Override
	public Object findNode(Object ast, int offset) {
		return null;
	}

	@Override
	public Object findNode(Object ast, int startOffset, int endOffset) {
		return null;
	}

	@Override
	public int getEndOffset(Object node) {
		return getStartOffset(node) + getLength(node) - 1;
	}

	@Override
	public int getLength(Object node) {
		return getLocation(node).getLength();
	}

	@Override
	public IPath getPath(Object node) {
		return null;
	}

	@Override
	public int getStartOffset(Object node) {
		return getLocation(node).getOffset();
	}
	
	private ISourceLocation getLocation(Object node) {
		if (node instanceof Token) {
			return ((Token) node).getLocation();
		}
		else if (node instanceof IConstructor) {
			return new TreeAdapter((IConstructor) node).getLocation();
		}
		else if (node instanceof AbstractAST) {
			return getLocation(((AbstractAST) node).getTree());
		}
		else if (node instanceof ModelTreeNode) {
			return getLocation(((ModelTreeNode) node).getASTNode());
		}
		throw new RuntimeException("Unknown node type " + node);
	}
}
