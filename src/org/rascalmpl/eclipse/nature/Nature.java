/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.nature;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.builder.ProjectNatureBase;
import org.eclipse.imp.runtime.IPluginLog;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;

public class Nature extends ProjectNatureBase implements IRascalResources {
	@Override
	public String getNatureID() {
		return IRascalResources.ID_RASCAL_NATURE;
	}
	
	@Override
	public String getBuilderID() {
		return IRascalResources.ID_RASCAL_BUILDER;
	}
	
	public void configure() throws CoreException {
		super.configure();
		IFolder folder = this.getProject().getFolder(IRascalResources.RASCAL_SRC);
		
		if (!folder.exists()) {
			folder.create(false, false, null);
		}
		
		new RascalEclipseManifest().createIfNotPresent(this.getProject());
	}

	@Override
	public IPluginLog getLog() {
		return Activator.getInstance();
	}

	@Override
	protected void refreshPrefs() { }
}
