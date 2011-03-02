package org.rascalmpl.eclipse.nature;

import java.io.PrintWriter;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.rascalmpl.eclipse.Activator;
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
				eval.reloadModules(names, URI.create("console:///"));
			}
			catch (Throwable x) {
				Activator.getInstance().logException("error while reloading", x);
			}
			
			dirtyModules.clear();
		}
	}

	public void finalize() {
		// make sure the resource listener is removed when we are garbage collected
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
	}
}
