/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Davy Landman  - Davy.Landman@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;

public interface PausableOutput extends Pausable {
	void output(byte[] b) throws IOException;
}
