package org.meta_environment.rascal.eclipse.editor;

import java.util.Iterator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.editor.ModelTreeNode;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.meta_environment.rascal.ast.AbstractAST;
import org.meta_environment.rascal.eclipse.outline.TreeModelBuilder.Group;
import org.meta_environment.uptr.TreeAdapter;

public class NodeLocator implements ISourcePositionLocator {

	public Object findNode(Object ast, int offset) {
		return null;
	}

	public Object findNode(Object ast, int startOffset, int endOffset) {
		return null;
	}

	public int getEndOffset(Object node) {
		return getStartOffset(node) + getLength(node) - 1;
	}

	public int getLength(Object node) {
		return getLocation(node).getLength();
	}

	public IPath getPath(Object node) {
		return null;
	}

	public int getStartOffset(Object node) {
		return getLocation(node).getOffset();
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
