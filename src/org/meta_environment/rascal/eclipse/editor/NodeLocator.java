package org.meta_environment.rascal.eclipse.editor;

import org.eclipse.core.runtime.IPath;
import org.eclipse.imp.parser.ISourcePositionLocator;

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
		return ((Token) node).getRange().getLength();
	}

	@Override
	public IPath getPath(Object node) {
		return null;
	}

	@Override
	public int getStartOffset(Object node) {
		return ((Token) node).getRange().getStartOffset();
	}

}
