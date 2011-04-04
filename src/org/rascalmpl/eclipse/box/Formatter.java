/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Bert Lisser - Bert.Lisser@cwi.nl (CWI)
*******************************************************************************/
package org.rascalmpl.eclipse.box;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.services.ISourceFormatter;
import org.rascalmpl.ast.Module;
import org.rascalmpl.eclipse.editor.ParseController;
import org.rascalmpl.interpreter.BoxEvaluator;
import org.rascalmpl.library.box.MakeBox;
import org.rascalmpl.parser.ASTBuilder;
import org.rascalmpl.values.uptr.TreeAdapter;

public class Formatter implements ISourceFormatter{
	
	BoxEvaluator eval = new BoxEvaluator();

	public void formatterStarts(String initialIndentation) {
		// TODO Auto-generated method stub
		
	}

	public String format(IParseController parseController, String content,
			boolean isLineStart, String indentation, int[] positions) {
		ParseController p = (ParseController) parseController;
		MakeBox makeBox = new MakeBox();
		IConstructor currentAST = (IConstructor) p.getCurrentAst();
		IList z = TreeAdapter.getArgs(currentAST);
		ASTBuilder astBuilder = new ASTBuilder();
		Module moduleAst = astBuilder.buildModule(currentAST);
		// System.err.println("computeBox: build");
		if (moduleAst != null) {
		IConstructor a = 	(IConstructor) eval.evalRascalModule(moduleAst, z);
		return makeBox.box2String(a);
		// System.err.println(a);
		}
		return content;
	}

	public void formatterStops() {
		// TODO Auto-generated method stub
		
	}

}
