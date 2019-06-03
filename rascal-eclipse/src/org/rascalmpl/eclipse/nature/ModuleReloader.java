/*******************************************************************************
 * Copyright (c) 2009-2015 CWI
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.util.ResourcesToModules;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.uri.URIUtil;

public class ModuleReloader{
	private final RascalModuleChangeListener moduleChangeListener;
	private final RascalModuleUpdateListener resourceChangeListener;
	
	private boolean destroyed;
	
	public ModuleReloader(IProject project, Evaluator eval, IWarningHandler warnings) {
		super();
		
		moduleChangeListener = new RascalModuleChangeListener(eval, project, warnings);
		resourceChangeListener = new RascalModuleUpdateListener(moduleChangeListener);
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
	}
	
	public void updateModules(IProgressMonitor monitor, IWarningHandler handler, Set<String> ignored){
		moduleChangeListener.clearMarkers();
		moduleChangeListener.updateModules(monitor, handler, ignored);
	}

	public synchronized void destroy(){
		if(destroyed) return;
		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
		
		destroyed = true;
	}
	
	protected void finalize(){
		destroy();
	}
	
	public static class RascalModuleUpdateListener implements IResourceChangeListener {
		private IModuleChangedListener interpreter;

		public RascalModuleUpdateListener(IModuleChangedListener interpreter) {
			this.interpreter = interpreter;
		}

		public void resourceChanged(IResourceChangeEvent event) {
			if(event.getDelta() == null)
				return;
			IResourceDelta[] deltas = event.getDelta().getAffectedChildren();

			try {
				for (IResourceDelta d : deltas) {
					d.accept(new IResourceDeltaVisitor() {
						public boolean visit(IResourceDelta delta)
								throws CoreException {
							IResource resource = delta.getResource();
							
							if (resource instanceof IFile) {
								IPath path = resource.getLocation();

								if (path != null && path.getFileExtension() != null && path.getFileExtension().equals(IRascalResources.RASCAL_EXT))  {
									switch (delta.getKind()) {
									case IResourceDelta.OPEN:
										break;
									case IResourceDelta.ADDED:
										break;
									case IResourceDelta.CHANGED:
										if (delta.getFlags() != IResourceDelta.MARKERS) {
											// only if its not just the markers
											notify(path);
										}
										break;
									case IResourceDelta.REMOVED:
										break;
									}
								}
								return false;
							}
							return true;
						}

						private void notify(IPath path) {
							IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
							String module = ResourcesToModules.moduleFromFile(file);
							
							if (module != null) {
							  interpreter.moduleChanged(module);
							}
						}
					});
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class RascalModuleChangeListener implements IModuleChangedListener{
		private final Set<String> dirtyModules = new HashSet<String>();
		private final Evaluator eval;
		private final IWarningHandler warnings;
		
		public RascalModuleChangeListener(Evaluator eval, IProject project, IWarningHandler warnings) {
			super();
			this.eval = eval;
			this.warnings = warnings;
		}
		
		public void clearMarkers() {
			warnings.clean();
		}

		public void moduleChanged(String name) {
			synchronized (dirtyModules) {
				dirtyModules.add(name);
			}
		}
		
		public void updateModules(IProgressMonitor monitor, IWarningHandler handler, Set<String> ignored) {
			synchronized (dirtyModules) {
				synchronized(eval){
					Set<String> todo = new HashSet<>();
					todo.addAll(dirtyModules);
					boolean changed = todo.removeAll(ignored);
					eval.reloadModules(new RascalMonitor(monitor, handler == null ? warnings : handler) , Collections.unmodifiableSet(todo), URIUtil.rootLocation("console"));
					dirtyModules.clear();
					if (changed) {
						dirtyModules.addAll(ignored);
					}
				}
			}
		}
	}
}
