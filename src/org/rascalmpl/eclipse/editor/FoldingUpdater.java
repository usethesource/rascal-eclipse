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

import java.util.HashMap;
import java.util.List;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.services.base.FolderBase;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.rascalmpl.values.uptr.TreeAdapter;

// TODO: this is work in progress, just started on this annotation driven folding updater
public class FoldingUpdater extends FolderBase {

	@Override
	protected void sendVisitorToAST(HashMap<Annotation, Position> newAnnotations, List<Annotation> annotations, Object ast) {
		if (ast instanceof IConstructor) {
			visitTree((IConstructor) ast);
		}
	}
	
	private void visitTree(IConstructor tree) {
		if (TreeAdapter.isAppl(tree)) {
			IValue fold = tree.getAnnotation("fold");

			if (fold != null) {
				ISourceLocation loc = TreeAdapter.getLocation(tree);
				
				if (loc == null) {
					throw new IllegalArgumentException("folder expects trees annotated with source locations");
				}
				
				makeAnnotation(loc.getOffset(), loc.getLength());
				return; // no support for nested folds!
			}
			
			for (IValue arg : TreeAdapter.getASTArgs(tree)) {
				if (!TreeAdapter.isLexical((IConstructor) arg)) {
					visitTree((IConstructor) arg);
				}
			}
		}
	}
}
