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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.TreeAdapter;

import io.usethesource.impulse.editor.ModelTreeNode;
import io.usethesource.impulse.language.ILanguageService;
import io.usethesource.impulse.services.ILabelProvider;
import io.usethesource.vallang.IBool;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IDateTime;
import io.usethesource.vallang.IExternalValue;
import io.usethesource.vallang.IInteger;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.IMap;
import io.usethesource.vallang.INode;
import io.usethesource.vallang.IRational;
import io.usethesource.vallang.IReal;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.visitors.IValueVisitor;

public class LabelProvider implements ILabelProvider, ILanguageService {
	private Set<ILabelProviderListener> fListeners = new HashSet<ILabelProviderListener>();
	
	public Image getImage(Object element) {
		return null;
	}

	public String getText(Object element) {
		if (element instanceof ModelTreeNode) {
			element = ((ModelTreeNode) element).getASTNode();
		}
		if (element instanceof IValue) {
				return ((IValue) element).accept(new IValueVisitor<String, RuntimeException>() {

				    @Override
					public String visitBoolean(IBool boolValue)  {
						return boolValue.toString();
					}

					@Override
					public String visitConstructor(IConstructor o) {
						IValue img = o.asWithKeywordParameters().getParameter("label");
						if (img != null) {
							if (img instanceof IString) {
								return ((IString) img).getValue();
							}
							
							return img.toString();
						}

						return o.getName();
					}

					@Override
					public String visitDateTime(IDateTime o)  {
						return o.toString();
					}

					@Override
					public String visitExternal(IExternalValue externalValue)
					 {
						return "";
					}

					@Override
					public String visitInteger(IInteger o)  {
						return o.toString();
					}

					@Override
					public String visitRational(IRational o)  {
						return o.toString();
					}

					@Override
					public String visitList(IList o)  {
						return "";
					}

					@Override
					public String visitMap(IMap o)  {
						return "";
					}

					public String visitNode(INode o)  {
						IValue label = o.asWithKeywordParameters().getParameter("label");
						if (label != null) {
							if (label instanceof IString) {
								return ((IString) label).getValue();
							}
							
							return label.toString();
						}

						return o.getName();
					}

					@Override
					public String visitReal(IReal o)  {
						return o.toString();
					}

					@Override
					public String visitSet(ISet o)  {
						return "";
					}

					@Override
					public String visitSourceLocation(ISourceLocation o)
					 {
						return o.toString();
					}

					@Override
					public String visitString(IString o)  {
						return o.getValue();
					}

					@Override
					public String visitTuple(ITuple o)  {
						return "";
					}

				});
		}
		
		if (element instanceof ITree) {
			return TreeAdapter.getSortName((ITree) element);
		}
		return "no-label";
	}

	public void addListener(ILabelProviderListener listener) {
		fListeners.add(listener);
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		fListeners.remove(listener);
	}
}
