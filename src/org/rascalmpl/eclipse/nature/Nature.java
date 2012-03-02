/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
*******************************************************************************/
package org.rascalmpl.eclipse.nature;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.builder.ProjectNatureBase;
import org.eclipse.imp.runtime.IPluginLog;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;

public class Nature extends ProjectNatureBase implements IRascalResources {
	private IProject project;

	@Override
	public String getNatureID() {
		return "rascal_eclipse.rascal_nature";
	}
	
	@Override
	public String getBuilderID() {
		return "rascal_eclipse.rascal_builder";
	}
	
	public void configure() throws CoreException {
		super.configure();
		
		IFolder folder = project.getFolder(IRascalResources.RASCAL_SRC);
		
		if (!folder.exists()) {
			folder.create(false, false, null);
		}
		
		link();
	}

	private void link() throws CoreException {
		try {
			IFolder lib = project.getFolder("std");
			
			if (!lib.exists()) {
				lib.createLink(new URI("rascal-library", RascalLibraryFileSystem.RASCAL, "", null), 0, null);
			}

			lib = project.getFolder("eclipse");
			
			if (!lib.exists()) {
				lib.createLink(new URI("rascal-library", RascalLibraryFileSystem.ECLIPSE, "", null), 0, null);
			}
		} catch (URISyntaxException e) {
			Activator.getInstance().logException("error during linking of libraries", e);
		}
	}

	@Override
	public IPluginLog getLog() {
		return Activator.getInstance();
	}

	@Override
	protected void refreshPrefs() { }
}
