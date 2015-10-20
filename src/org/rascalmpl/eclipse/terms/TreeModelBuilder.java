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

import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.types.RascalTypeFactory;
import org.rascalmpl.value.IBool;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.IDateTime;
import org.rascalmpl.value.IExternalValue;
import org.rascalmpl.value.IInteger;
import org.rascalmpl.value.IList;
import org.rascalmpl.value.IMap;
import org.rascalmpl.value.INode;
import org.rascalmpl.value.IRational;
import org.rascalmpl.value.IReal;
import org.rascalmpl.value.ISet;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IString;
import org.rascalmpl.value.ITuple;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.type.Type;
import org.rascalmpl.value.visitors.IValueVisitor;
import org.rascalmpl.values.uptr.ITree;

import io.usethesource.impulse.language.ILanguageService;
import io.usethesource.impulse.language.Language;
import io.usethesource.impulse.services.base.TreeModelBuilderBase;

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
