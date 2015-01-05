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
 
import java.io.PrintWriter;

import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.MessagesToAnnotations;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.interpreter.types.RascalTypeFactory;
import org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages;
import org.rascalmpl.parser.gtd.exception.ParseError;
import org.rascalmpl.values.uptr.TreeAdapter;

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
	
	public synchronized IConstructor annotate(ICallableValue func, IConstructor parseTree, IMessageHandler handler) {
		try {
			IConstructor top = parseTree;
			boolean start = false;
			IConstructor tree;
			if (TreeAdapter.isAppl(top) && TreeAdapter.getSortName(top).equals("<START>")) {
				tree = (IConstructor) TreeAdapter.getArgs(top).get(1);
				start = true;
			}
			else {
				tree = top;
			}
			
			Type type = RascalTypeFactory.getInstance().nonTerminalType(tree);
			IConstructor newTree;
			synchronized(func.getEval()){
				func.getEval().__setInterrupt(false);
				newTree = (IConstructor) func.call(new Type[] {type}, new IValue[] {tree}, null).getValue();
			}
			
			if (newTree != null) {
				if (start) {
					IList newArgs = TreeAdapter.getArgs(top).put(1, newTree);
					newTree = TreeAdapter.setLocation(TreeAdapter.setArgs(top, newArgs),TreeAdapter.getLocation(top));
				}
				marker.process(newTree, handler);
				return newTree;
			}
			else {
				Activator.getInstance().logException("annotator returned null", new RuntimeException());
			}
		}
		catch (RuntimeException e) {
			if (e instanceof ParseError || e instanceof StaticError || e instanceof Throw) {
				PrintWriter stdErr = func.getEval().getStdErr();
				stdErr.write("Annotator failed\n");
				stdErr.write(ReadEvalPrintDialogMessages.parseOrStaticOrThrowMessage(e));
				stdErr.flush();
			}
			else {
				Activator.getInstance().logException("annotater failed", e);
			}
		}
		catch (Throwable e) {
			Activator.getInstance().logException("annotater failed", e);
		}
		
		return null;
	}
}
