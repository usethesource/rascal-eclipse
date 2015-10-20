/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Various members of the Software Analysis and Transformation Group - CWI
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package org.rascalmpl.eclipse.terms;

import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;

import io.usethesource.impulse.builder.ProjectNatureBase;
import io.usethesource.impulse.runtime.IPluginLog;

public class TermNature extends ProjectNatureBase {

	@Override
	public String getNatureID() {
		return IRascalResources.ID_TERM_NATURE;
	}

	@Override
	public String getBuilderID() {
		return IRascalResources.ID_TERM_BUILDER;
	}

	@Override
	public IPluginLog getLog() {
		return Activator.getInstance();
	}

	@Override
	protected void refreshPrefs() {
		// don't know
	}
}
