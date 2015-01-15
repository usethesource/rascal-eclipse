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

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.services.IDocumentationProvider;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.TreeAdapter;

/*
 * Assuming the innermost lexical node in the parse tree is given,  we simply return the annotation labeled
 * "doc" which is a string.
 */
public class DocumentationProvider  implements IDocumentationProvider {
	
	public String getDocumentation(Object target,
			IParseController parseController) {
		if (target instanceof IConstructor) {
			if (((IConstructor) target).getType() == Factory.Tree) {
				return getDocString((IConstructor) target, (IConstructor) parseController.getCurrentAst());
			}
		}
		
		return null;
	}

	private String getDocString(IConstructor arg, IConstructor top) {
		IValue val = arg.asWithKeywordParameters().getParameter("doc");

		if (val != null && val.getType().isString()) {
				return ((IString) val).getValue();
		}
		
		if (top != null && top.getType() == Factory.Tree) {
			IValue vals = arg.asWithKeywordParameters().getParameter("docs");
			
			if (vals != null 
					&& vals.getType().isMap() 
					&& vals.getType().getKeyType().isSourceLocation() 
					&& vals.getType().getValueType().isString()) {
				IMap map = (IMap) vals;
				ISourceLocation loc = TreeAdapter.getLocation(arg);
				if (loc != null) {
					return ((IString) map.get(loc)).getValue();
				}
			}

			IValue docStringsMapValue = top.asWithKeywordParameters().getParameter("docStrings");
			IValue loc = TreeAdapter.getLocation(arg);
			if (docStringsMapValue != null && docStringsMapValue.getType().isMap() && loc != null && loc.getType().isSourceLocation()) {
				IMap docStringsMap = (IMap)docStringsMapValue;
				if (docStringsMap.containsKey(loc)) { 
					IValue docString = docStringsMap.get(loc);
					if (docString.getType().isString()) return ((IString)docString).getValue();
				}
			}
		}
		
		return null;
	}
}	
