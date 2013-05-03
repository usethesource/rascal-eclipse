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

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;
import org.eclipse.imp.services.base.FolderBase;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.ProductionAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;
import org.rascalmpl.values.uptr.visitors.TreeVisitor;

public class FoldingUpdater extends FolderBase {

	@Override
	protected void sendVisitorToAST(
			HashMap<Annotation, Position> newAnnotations,
			List<Annotation> annotations, Object ast) {
		if (ast instanceof IConstructor) {
			try {
				((IConstructor) ast).accept(new TreeVisitor() {
					public IConstructor visitTreeCycle(IConstructor arg)
							throws VisitorException {
						return null;
					}
					
					@Override
					public IConstructor visitTreeChar(IConstructor arg) throws VisitorException {
						return null;
					}
					
					@Override
					public IConstructor visitTreeAppl(IConstructor arg) throws VisitorException {
						IConstructor prod = TreeAdapter.getProduction(arg);
						IValueFactory VF = ValueFactoryFactory.getValueFactory();
						
						if (ProductionAdapter.hasAttribute(prod, VF.constructor(Factory.Attr_Tag, VF.node("Foldable")))) {
							makeAnnotation(arg, false);	
						}
						else if (ProductionAdapter.hasAttribute(prod, VF.constructor(Factory.Attr_Tag, VF.node("Folded")))) {
							makeAnnotation(arg, true);	
						}
						else if (arg.getAnnotation("foldable") != null) {
							makeAnnotation(arg, false);
						}
						else if (arg.getAnnotation("folded") != null) {
							makeAnnotation(arg, true);
						}
						
						if (!TreeAdapter.isLexical(arg)) {
							for (IValue kid :  TreeAdapter.getASTArgs(arg)) {
								kid.accept(this);
							}
						}
						
						return null;
					}
					
					public IConstructor visitTreeAmb(IConstructor arg) throws VisitorException {
						return null;
					}
				});
			} catch (VisitorException e) {
				Activator.getInstance().logException(e.getMessage(), e);
			}
		}
	}
	
	@Override
	public void makeAnnotation(Object arg, boolean folded) {
		IConstructor c = (IConstructor) arg;
		ISourceLocation l = TreeAdapter.getLocation(c);
		
		if (l != null && l.getBeginLine() != l.getEndLine()) {
			super.makeAnnotation(arg, folded);
		}
	}
}
