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

import io.usethesource.impulse.editor.ModelTreeNode;
import io.usethesource.impulse.language.ILanguageService;
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
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import io.usethesource.impulse.services.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.TreeAdapter;

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

					public String visitBoolean(IBool boolValue)  {
						return boolValue.toString();
					}

					public String visitConstructor(IConstructor o)
					 {
						IValue img = o.asAnnotatable().getAnnotation("label");
						if (img != null) {
							if (img instanceof IString) {
								return ((IString) img).getValue();
							}
							
							return img.toString();
						}

						return o.getName();
					}

					public String visitDateTime(IDateTime o)  {
						return o.toString();
					}

					public String visitExternal(IExternalValue externalValue)
					 {
						return "";
					}

					public String visitInteger(IInteger o)  {
						return o.toString();
					}

					public String visitRational(IRational o)  {
						return o.toString();
					}

					public String visitList(IList o)  {
						return "";
					}

					public String visitMap(IMap o)  {
						return "";
					}

					public String visitNode(INode o)  {
						IValue label = o.asAnnotatable().getAnnotation("label");
						if (label != null) {
							if (label instanceof IString) {
								return ((IString) label).getValue();
							}
							
							return label.toString();
						}

						return o.getName();
					}

					public String visitReal(IReal o)  {
						return o.toString();
					}

					public String visitRelation(ISet o)  {
						return "";
					}
					
					public String visitListRelation(IList o)  {
						return "";
					}

					public String visitSet(ISet o)  {
						return "";
					}

					public String visitSourceLocation(ISourceLocation o)
					 {
						return o.toString();
					}

					public String visitString(IString o)  {
						return o.getValue();
					}

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
