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

import org.rascalmpl.value.IInteger;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.IValueFactory;

public class ValueUI {
	public ValueUI(IValueFactory vf) { }

	public void text(IValue v, IInteger tabsize) {
		org.rascalmpl.eclipse.views.values.text.Editor.edit(v, true, tabsize.intValue());
	}

	public void tree(IValue v) {
		org.rascalmpl.eclipse.views.values.tree.Editor.open(v);
	}
}
