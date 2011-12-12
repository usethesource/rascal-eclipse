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
		IValue val = arg.getAnnotation("doc");

		if (val != null && val.getType().isStringType()) {
				return ((IString) val).getValue();
		}
		
		if (top != null && top.getType() == Factory.Tree) {
			IValue vals = arg.getAnnotation("docs");
			
			if (vals != null 
					&& vals.getType().isMapType() 
					&& vals.getType().getKeyType().isSourceLocationType() 
					&& vals.getType().getValueType().isStringType()) {
				IMap map = (IMap) vals;
				ISourceLocation loc = (ISourceLocation) arg.getAnnotation("loc");
				if (loc != null) {
					return ((IString) map.get(loc)).getValue();
				}
			}

			IValue docStringsMapValue = top.getAnnotation("docStrings");
			IValue loc = arg.getAnnotation("loc");
			if (docStringsMapValue != null && docStringsMapValue.getType().isMapType() && loc != null && loc.getType().isSourceLocationType()) {
				IMap docStringsMap = (IMap)docStringsMapValue;
				if (docStringsMap.containsKey(loc)) { 
					IValue docString = docStringsMap.get(loc);
					if (docString.getType().isStringType()) return ((IString)docString).getValue();
				}
			}
		}
		
		return null;
	}
}	
