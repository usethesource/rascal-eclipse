/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Tijs van der Storm - Tijs.van.der.Storm@cwi.nl
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.terms;
 
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.MessagesToAnnotations;
import org.rascalmpl.values.functions.IFunction;
import org.rascalmpl.values.parsetrees.ITree;
import org.rascalmpl.values.parsetrees.TreeAdapter;

import io.usethesource.impulse.parser.IMessageHandler;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;

/**
 * This class connects the Eclipse IDE with Rascal functions that annotate parse trees.
 * 
 * It makes sure these annotator functions are called after each successful parse.
 * After proper annotations have been added, features such as documentation tooltips
 * and hyperlinking uses to definitions start working.
 * 
 * Note that this class only works for languages that have been registered using the
 * API in SourceEditor.rsc
 */
public class AnnotatorExecutor {
	private final MessagesToAnnotations marker = new MessagesToAnnotations();
	
	public synchronized ITree annotate(IFunction func, ITree parseTree, IMessageHandler handler) {
		try {
			ITree top = parseTree;
			boolean start = false;
			IConstructor tree;
			if (TreeAdapter.isAppl(top) && TreeAdapter.getSortName(top).equals("<START>")) {
				tree = (IConstructor) TreeAdapter.getArgs(top).get(1);
				start = true;
			}
			else {
				tree = top;
			}
			
			ITree newTree = (ITree) func.call(tree);
			
			if (newTree != null) {
				if (start) {
					IList newArgs = TreeAdapter.getArgs(top).put(1, newTree);
					newTree = (ITree) top.set("args", newArgs).asWithKeywordParameters().setParameter("src", top.asWithKeywordParameters().getParameter("src"));
				}
				marker.process(newTree, handler);
				return newTree;
			}
			else {
				Activator.getInstance().logException("annotator returned null", new RuntimeException());
			}
		}
		catch (Throwable e) {
			Activator.getInstance().logException("annotater failed", e);
		}
		
		return null;
	}
}
