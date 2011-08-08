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
import java.net.URISyntaxException;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import org.rascalmpl.interpreter.Evaluator;

public class ModuleReloader{
	private final RascalModuleChangeListener moduleChangeListener;
	private final RascalModuleUpdateListener resourceChangeListener;
	
	private boolean destroyed;
	
	public ModuleReloader(Evaluator eval) {
		super();
		
		moduleChangeListener = new RascalModuleChangeListener(eval);
		resourceChangeListener = new RascalModuleUpdateListener(moduleChangeListener);
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
	}
	
	public void updateModules(IProgressMonitor monitor){
		moduleChangeListener.updateModules(monitor);
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
							IProject proj = file.getProject();
							if (proj != null && proj.exists()) {
								IFolder srcFolder = proj.getFolder(IRascalResources.RASCAL_SRC);
								if (srcFolder != null && srcFolder.exists()) {
									if (srcFolder.getProjectRelativePath().isPrefixOf(file.getProjectRelativePath())) {
										try{
											URI uri = new URI("project://" + proj.getName() + "/" + file.getProjectRelativePath().removeFirstSegments(1).toPortableString());
											interpreter.moduleChanged(uri);
											uri = new URI("project://" + proj.getName() + "/" + IRascalResources.RASCAL_SRC + "/" + file.getProjectRelativePath().removeFirstSegments(1).toPortableString());
											interpreter.moduleChanged(uri);
										}catch(URISyntaxException usex){
											usex.printStackTrace(); // TODO Change to something better.
										}
									}
								}
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
		private final HashSet<URI> dirtyModules = new HashSet<URI>();
		private final Evaluator eval;
		
		public RascalModuleChangeListener(Evaluator eval){
			super();
			
			this.eval = eval;
		}
		
		public void moduleChanged(URI name) {
			synchronized (dirtyModules) {
				dirtyModules.add(name);
			}
		}
		
		public void updateModules(IProgressMonitor monitor) {
			synchronized (dirtyModules) {
				HashSet<String> names = new HashSet<String>();
				
				for (URI uri : dirtyModules) {
					String path = uri.getPath();
					path = path.substring(0, path.indexOf(IRascalResources.RASCAL_EXT) - 1);
					path = path.startsWith("/") ? path.substring(1) : path;
					names.add(path.replaceAll("/","::"));
				}
				
				dirtyModules.clear();
				
				
				try {
					synchronized(eval){
						eval.reloadModules(new RascalMonitor(monitor) , names, URI.create("console:///"));
					}
				}catch (Throwable x) {
					// reloading modules may trigger many issues, however, these should be visible
					// already in the editors for the respective modules, so we ignore them here
					// to prevent redundant error messages
				}
			}
		}
	}
}
