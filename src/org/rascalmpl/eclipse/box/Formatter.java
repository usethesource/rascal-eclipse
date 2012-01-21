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
import org.eclipse.imp.services.ISourceFormatter;

public class Formatter implements ISourceFormatter{
	
//	BoxEvaluator eval = new BoxEvaluator();

	public void formatterStarts(String initialIndentation) {
		// TODO Auto-generated method stub
		
	}

	public String format(IParseController parseController, String content,
			boolean isLineStart, String indentation, int[] positions) {
		throw new RuntimeException("The Rascal formatter is temporarily unavailable");
	}

	public void formatterStops() {
		// TODO Auto-generated method stub
		
	}

}
