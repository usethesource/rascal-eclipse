package org.rascalmpl.eclipse.perspective.actions;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.rascalmpl.checker.StaticChecker;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.eclipse.uri.BundleURIResolver;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;
import org.rascalmpl.uri.ClassResourceInputOutput;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;

public class StaticCheckerHelper {
	private static HashMap<ISourceProject, StaticChecker> checkerMap = new HashMap<ISourceProject, StaticChecker>();
	
	public void initChecker(StaticChecker checker, final ISourceProject sourceProject) {
		checker.init();

		List<String> srcs = new RascalEclipseManifest().getSourceRoots(sourceProject.getRawProject());
    
		try {
		  if (srcs != null) {
		    for (String root : srcs) {
		      checker.addRascalSearchPath(URIUtil.create("project", sourceProject.getRawProject().getName(), "/" + root.trim()));
		    }
		  }
		  else if (sourceProject.getRawProject().exists(new Path(IRascalResources.RASCAL_SRC))) {
		    checker.addRascalSearchPath(URIUtil.create("project", sourceProject.getRawProject().getName(), "/" + IRascalResources.RASCAL_SRC));
		  }
		  else {
		    checker.addRascalSearchPath(URIUtil.create("project", sourceProject.getRawProject().getName(), "/"));
		  }
		} catch (URISyntaxException e) {
		  Activator.log("???", e);
		}
		finally {}
		
		ProjectURIResolver resolver = new ProjectURIResolver();
		URIResolverRegistry resolverRegistry = checker.getResolverRegistry();
		resolverRegistry.registerInput(resolver);
		resolverRegistry.registerOutput(resolver);
		
		ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(resolverRegistry, "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
		resolverRegistry.registerInput(eclipseResolver);
		checker.addRascalSearchPath(URIUtil.rootScheme(eclipseResolver.scheme()));
		checker.addClassLoader(getClass().getClassLoader());
		
		BundleURIResolver bundleResolver = new BundleURIResolver(resolverRegistry);
		resolverRegistry.registerInput(bundleResolver);
		resolverRegistry.registerOutput(bundleResolver);

		try {
			String rascalPlugin = FileLocator.resolve(Platform.getBundle("rascal").getEntry("/")).getPath();
			String PDBValuesPlugin = FileLocator.resolve(Platform.getBundle("org.eclipse.imp.pdb.values").getEntry("/")).getPath();
			checker.getConfiguration().setRascalJavaClassPathProperty(rascalPlugin + File.pathSeparator + PDBValuesPlugin + File.pathSeparator + rascalPlugin + File.separator + "src" + File.pathSeparator + rascalPlugin + File.separator + "bin" + File.pathSeparator + PDBValuesPlugin + File.separator + "bin");
		} catch (IOException e) {
			Activator.getInstance().logException("could not create classpath for parser compilation", e);
		}

		checker.enableChecker(null);
	}
	
	public StaticChecker createChecker(ISourceProject sourceProject) {
		PrintStream consoleStream = RuntimePlugin.getInstance().getConsoleStream();
		StaticChecker checker = new StaticChecker(new PrintWriter(consoleStream), new PrintWriter(consoleStream));
		checkerMap.put(sourceProject, checker);
		initChecker(checker, sourceProject);
		return checker;
	}

	public StaticChecker createCheckerIfNeeded(ISourceProject sourceProject) {
		StaticChecker checker = null;
		if (checkerMap.containsKey(sourceProject)) {
			checker = checkerMap.get(sourceProject);
		}
		if (checker == null) {
			checker = createChecker(sourceProject);
		}
		return checker;
	}
	
	public StaticChecker reloadChecker(ISourceProject sourceProject) {
		StaticChecker checker = null;
		if (checkerMap.containsKey(sourceProject)) {
			checkerMap.remove(sourceProject);
		}
		checker = createChecker(sourceProject);
		return checker;
	}
}