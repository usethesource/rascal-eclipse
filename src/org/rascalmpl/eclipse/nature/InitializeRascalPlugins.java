package org.rascalmpl.eclipse.nature;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IStartup;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.IRascalSearchPathContributor;
import org.rascalmpl.uri.ClassResourceInputOutput;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.ValueFactoryFactory;

public class InitializeRascalPlugins implements IStartup {
	public void earlyStartup() {
		registerTermLanguagePlugins();
	}
	
	public static void registerTermLanguagePlugins() {
		for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			registerTermLanguagePlugin(project);
		}
	}

	public static void registerTermLanguagePlugin(final IProject project) {
		try {
			if (project.hasNature(Nature.getNatureId())) {
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
			eval.doImport("Plugin");
			eval.call("main");
		} 
		catch (Throwable e) {
			Activator.getInstance().logException("could not run plugin main", e);
		}
	}

	private static Evaluator initializeEvaluator(final IProject project) {
		Evaluator eval = new Evaluator(ValueFactoryFactory.getValueFactory(), new PrintWriter(System.err), new PrintWriter(System.out), new ModuleEnvironment("***plugin starter***"), new GlobalEnvironment());
		
		eval.addRascalSearchPathContributor(new IRascalSearchPathContributor() {
			public void contributePaths(List<URI> path) {
				try{
					path.add(0, new URI("project://" + project.getName() + "/" + IRascalResources.RASCAL_SRC));
				} catch (URISyntaxException usex){
					Activator.getInstance().logException("uri syntax exception", usex);
				}
			}
		});
		
		ProjectURIResolver resolver = new ProjectURIResolver();
		URIResolverRegistry resolverRegistry = eval.getResolverRegistry();
		resolverRegistry.registerInput(resolver);
		resolverRegistry.registerOutput(resolver);
		
		ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(resolverRegistry, "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
		resolverRegistry.registerInput(eclipseResolver);
		eval.addRascalSearchPath(URI.create(eclipseResolver.scheme() + ":///"));
		eval.addClassLoader(Activator.getInstance().getClass().getClassLoader());
		
		try {
			String rascalPlugin = FileLocator.resolve(Platform.getBundle("rascal").getEntry("/")).getPath();
			String PDBValuesPlugin = FileLocator.resolve(Platform.getBundle("org.eclipse.imp.pdb.values").getEntry("/")).getPath();
			Configuration.setRascalJavaClassPathProperty(rascalPlugin + File.pathSeparator + PDBValuesPlugin + File.pathSeparator + rascalPlugin + File.separator + "src" + File.pathSeparator + rascalPlugin + File.separator + "bin" + File.pathSeparator + PDBValuesPlugin + File.separator + "bin");
		} catch (IOException e) {
			Activator.getInstance().logException("could not create classpath for parser compilation", e);
		}
		
		return eval;
	}
	

}
