/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.nature;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.interpreter.Evaluator;

public class ModuleReloader implements IModuleChangedListener {
	private final Set<URI> dirtyModules = new HashSet<URI>();
	private final RascalModuleUpdateListener resourceChangeListener;
	private final Evaluator eval;
	
	public ModuleReloader(Evaluator eval) {
		this.eval = eval;
		this.resourceChangeListener = new RascalModuleUpdateListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
	}
	
	public  void moduleChanged(URI name) {
		synchronized (dirtyModules) {
			dirtyModules.add(name);
		}
	}
	
	public void updateModules() {
		synchronized (dirtyModules) {
			Set<String> names = new HashSet<String>();
			
			for (URI uri : dirtyModules) {
				String path = uri.getPath();
				path = path.substring(0, path.indexOf(IRascalResources.RASCAL_EXT) - 1);
				path = path.startsWith("/") ? path.substring(1) : path;
				names.add(path.replaceAll("/","::"));
			}
			
			try {
				eval.reloadModules(eval.getMonitor(), names, URI.create("console:///"));
			}
			catch (Throwable x) {
				// reloading modules may trigger many issues, however, these should be visible
				// already in the editors for the respective modules, so we ignore them here
				// to prevent redundant error messages
			}
			
			dirtyModules.clear();
		}
	}

	public void finalize() {
		// make sure the resource listener is removed when we are garbage collected
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
	}
}
