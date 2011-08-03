/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Davy Landman - Davy.Landman@cwi.nl - CWI
 *******************************************************************************/
package org.rascalmpl.eclipse.library.vis;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.interpreter.IEvaluatorContext;

public class FigureLibrary {
	
	IValueFactory values;

	public FigureLibrary(IValueFactory values) {
		this.values = values;
	}
	
	public void renderActual(IString name, IConstructor fig, IEvaluatorContext ctx) {
		FigureViewer.open(name, fig, ctx);
	}
	
	public void render(IList fig, IEvaluatorContext ctx) {
		FigureViewer.open(values.string("Figure"), fig, ctx);
	}

	public void render(IString name, IList fig, IEvaluatorContext ctx) {
		FigureViewer.open(name, fig, ctx);
	}

}
