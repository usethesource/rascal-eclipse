/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.terms;

import org.eclipse.imp.language.ILanguageService;
import org.eclipse.imp.language.Language;
import org.eclipse.imp.pdb.facts.IBool;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IDateTime;
import org.eclipse.imp.pdb.facts.IExternalValue;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.INode;
import org.eclipse.imp.pdb.facts.IRational;
import org.eclipse.imp.pdb.facts.IReal;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.ITuple;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.services.base.TreeModelBuilderBase;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.types.RascalTypeFactory;
import org.rascalmpl.values.uptr.ITree;

public class TreeModelBuilder extends TreeModelBuilderBase implements ILanguageService{
	private Language lang;
	
	public TreeModelBuilder(){
		super();
	}

	@Override
	protected void visitTree(Object root) {
		Language lang = initLanguage(root);

		if (lang == null || root == null) return;

		IConstructor pt = (IConstructor) root;
		ICallableValue outliner = TermLanguageRegistry.getInstance().getOutliner(lang);

		if (outliner == null) {
			return;
		}

		try {
			IValue outline;
			synchronized(outliner.getEval()){
				outline = outliner.call(new Type[] {RascalTypeFactory.getInstance().nonTerminalType(pt)}, new IValue[] {pt}, null).getValue();
			}

			if (outline instanceof INode) {
				INode node = (INode) outline;
				createTopItem(outline);

					for (IValue child : node) {
						child.accept(new IValueVisitor<Object, RuntimeException>() {
							public Object visitBoolean(IBool o)
							 {
								createSubItem(o);
								return null;
							}

							public Object visitConstructor(IConstructor o)
							 {
								pushSubItem(o);
								for (IValue child : o) {
									child.accept(this);
								}
								popSubItem();
								return null;
							}

							public Object visitDateTime(IDateTime o)
							 {
								createSubItem(o);
								return null;
							}

							public Object visitExternal(IExternalValue o)
							 {
								createSubItem(o);
								return null;
							}

							public Object visitInteger(IInteger o)  {
								createSubItem(o);
								return null;
							}

							public Object visitRational(IRational o)  {
								createSubItem(o);
								return null;
							}

							public Object visitList(IList o)  {
								for (IValue elem : o) {
									elem.accept(this);
								}
								return null;
							}

							public Object visitMap(IMap o)  {
								for (IValue key : o) {
									pushSubItem(key);
									o.get(key).accept(this);
									popSubItem();
								}
								return null;
							}

							public Object visitNode(INode o)  {
								pushSubItem(o);
								for (IValue child : o) {
									child.accept(this);
								}
								popSubItem();
								return null;
							}

							public Object visitReal(IReal o)  {
								return createSubItem(o);
							}

							public Object visitRelation(ISet o)
							 {
								for (IValue tuple : o) {
									tuple.accept(this);
								}
								return null;
							}
							
							public Object visitListRelation(IList o)
							 {
								for (IValue tuple : o) {
										tuple.accept(this);
								}
								return null;
							}

							public Object visitSet(ISet o)  {
								for (IValue tuple : o) {
									tuple.accept(this);
								}
								return null;
							}

							public Object visitSourceLocation(ISourceLocation o)
							 {
								return createSubItem(o);
							}

							public Object visitString(IString o)  {
								return createSubItem(o);
							}

							public Object visitTuple(ITuple o)  {
								for (IValue field : o) {
									field.accept(this);
								}
								return null;
							}
						});

					}
			}
		}
		catch (Throwable e) {
			Activator.getInstance().logException("outliner failed: " + e.getMessage(), e);
			return;
		}
	}

	private Language initLanguage(Object root) {
		if (lang == null) {
			if (root instanceof ITree) {
				lang = TermLanguageRegistry.getInstance().getLanguage((ITree) root);
			}
		}
		return lang;
	}
}
