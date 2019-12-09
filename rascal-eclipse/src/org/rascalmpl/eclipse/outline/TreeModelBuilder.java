/*******************************************************************************
 * Copyright (c) 2009-2017 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.outline;

import org.rascalmpl.eclipse.editor.RascalLanguageServices;

import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.INode;

public class TreeModelBuilder extends org.rascalmpl.eclipse.terms.TreeModelBuilder {

	@Override
	protected void visitTree(Object root) {
	    if (root == null) {
	        return;
	    }
	    INode model = RascalLanguageServices.getInstance().getOutline((IConstructor) root);
	    convertModel(model);
	}
}
