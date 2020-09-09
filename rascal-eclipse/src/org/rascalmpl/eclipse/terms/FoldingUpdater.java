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
package org.rascalmpl.eclipse.terms;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.rascalmpl.values.RascalValueFactory;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.parsetrees.ITree;
import org.rascalmpl.values.parsetrees.ProductionAdapter;
import org.rascalmpl.values.parsetrees.TreeAdapter;
import org.rascalmpl.values.parsetrees.visitors.TreeVisitor;

import io.usethesource.impulse.services.base.FolderBase;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;

public class FoldingUpdater extends FolderBase {

	@Override
	protected void sendVisitorToAST(
			HashMap<Annotation, Position> newAnnotations,
			List<Annotation> annotations, Object ast) {
		if (ast instanceof ITree) {
				((ITree) ast).accept(new TreeVisitor<RuntimeException>() {
					public ITree visitTreeCycle(ITree arg)
							 {
						return null;
					}
					
					@Override
					public ITree visitTreeChar(ITree arg)  {
						return null;
					}
					
					@Override
					public ITree visitTreeAppl(ITree arg)  {
						IConstructor prod = TreeAdapter.getProduction(arg);
						IValueFactory VF = ValueFactoryFactory.getValueFactory();
						
						if (ProductionAdapter.hasAttribute(prod, VF.constructor(RascalValueFactory.Attr_Tag, VF.node("Foldable")))) {
							makeAnnotation(arg, false);	
						}
						else if (ProductionAdapter.hasAttribute(prod, VF.constructor(RascalValueFactory.Attr_Tag, VF.node("Folded")))) {
							makeAnnotation(arg, true);	
						}
						else if (arg.asWithKeywordParameters().getParameter("foldable") != null) {
							makeAnnotation(arg, false);
						}
						else if (arg.asWithKeywordParameters().getParameter("folded") != null) {
							makeAnnotation(arg, true);
						}
						
						if (!TreeAdapter.isLexical(arg)) {
							for (IValue kid :  TreeAdapter.getASTArgs(arg)) {
								kid.accept(this);
							}
						}
						
						return null;
					}
					
					public ITree visitTreeAmb(ITree arg)  {
						return null;
					}
				});
		}
	}
	
	@Override
	public void makeAnnotation(Object arg, boolean folded) {
		ITree c = (ITree) arg;
		ISourceLocation l = TreeAdapter.getLocation(c);
		
		if (l != null && l.getBeginLine() != l.getEndLine()) {
			super.makeAnnotation(arg, folded);
		}
	}
}
