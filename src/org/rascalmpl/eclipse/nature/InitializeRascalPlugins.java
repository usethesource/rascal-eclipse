package org.rascalmpl.eclipse.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.language.ILanguageRegistrar;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.NullRascalMonitor;

public class InitializeRascalPlugins implements ILanguageRegistrar {
	public void registerLanguages() {
			registerTermLanguagePlugins();
	}
	
	public static void registerTermLanguagePlugins() {
		for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			registerTermLanguagePlugin(project);
		}
	}

	public static void registerTermLanguagePlugin(final IProject project) {
		try {
			if (project.isOpen() && project.hasNature(Nature.getNatureId())) {
				IResource pluginRsc = project.findMember("src/Plugin.rsc");
				if (pluginRsc != null) {
					runPluginMain(project);
				}
			}
		}
		catch (CoreException e) {
			Activator.getInstance().logException("could not register any term language plugins", e);
		}
	}

	private static void runPluginMain(final IProject project) {
		try {
			Evaluator eval = initializeEvaluator(project);
			eval.doImport(null, "Plugin");
			eval.call(new NullRascalMonitor(), "main");
		}
		catch (Throwable e) {
			Activator.getInstance().logException("could not run Plugin.rsc main of " + project.getName(), e);
		}
	}

	private static Evaluator initializeEvaluator(final IProject project) {
		return ProjectEvaluatorFactory.getInstance().createProjectEvaluator(project);
	}
}
