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

import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.MessagesToAnnotations;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.interpreter.types.RascalTypeFactory;
import org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages;
import org.rascalmpl.parser.gtd.exception.ParseError;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.TreeAdapter;

import io.usethesource.impulse.parser.IMessageHandler;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.io.StandardTextWriter;
import io.usethesource.vallang.type.Type;

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
	
	public synchronized ITree annotate(ICallableValue func, ITree parseTree, IMessageHandler handler) {
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
			
			Type type = RascalTypeFactory.getInstance().nonTerminalType(tree);
			ITree newTree;
			synchronized(func.getEval()){
				func.getEval().__setInterrupt(false);
				newTree = (ITree) func.call(new Type[] {type}, new IValue[] {tree}, null).getValue();
			}
			
			if (newTree != null) {
				if (start) {
					IList newArgs = TreeAdapter.getArgs(top).put(1, newTree);
					newTree = (ITree) top.set("args", newArgs).asAnnotatable().setAnnotation("loc", top.asAnnotatable().getAnnotation("loc"));
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
				ReadEvalPrintDialogMessages.parseOrStaticOrThrowMessage(stdErr, e, new StandardTextWriter(true));
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
