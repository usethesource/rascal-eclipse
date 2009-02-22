package org.meta_environment.rascal.eclipse.editor;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.parser.ISourcePositionLocator;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceRange;
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
		return getRange(node).getLength();
	}

	@Override
	public IPath getPath(Object node) {
		return null;
	}

	@Override
	public int getStartOffset(Object node) {
		return getRange(node).getStartOffset();
	}
	
	private ISourceRange getRange(Object node) {
		if (node instanceof Token) {
			return ((Token) node).getRange();
		}
		else if (node instanceof IConstructor) {
			return new TreeAdapter((IConstructor) node).getRange();
		}
		throw new RuntimeException("Unknown node type " + node);
	}
}
