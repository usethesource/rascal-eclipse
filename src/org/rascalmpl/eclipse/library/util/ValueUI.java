/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.library.util;

import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;

public class ValueUI {
	public ValueUI(IValueFactory vf) { }

	public void text(IValue v, IInteger tabsize) {
		org.eclipse.imp.pdb.ui.text.Editor.edit(v, true, tabsize.intValue());
	}

	public void tree(IValue v) {
		org.eclipse.imp.pdb.ui.tree.Editor.open(v);
	}
}
