/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.nature;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.eclipse.console.internal.StdAndErrorViewPart;
import org.rascalmpl.eclipse.uri.BootstrapURIResolver;
import org.rascalmpl.eclipse.uri.BundleURIResolver;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.uri.ClassResourceInputOutput;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;

public class ProjectEvaluatorFactory {
	
	private final WeakHashMap<IProject, Evaluator> parserForProject = new WeakHashMap<IProject, Evaluator>();
	private final WeakHashMap<IProject, ModuleReloader> reloaderForProject = new WeakHashMap<IProject, ModuleReloader>();
	private final PrintWriter out;
	private final PrintWriter err;
	
	private ProjectEvaluatorFactory() {
		try {
			out = new PrintWriter(new OutputStreamWriter(StdAndErrorViewPart.getStdOut(), "UTF16"));
			err = new PrintWriter(new OutputStreamWriter(StdAndErrorViewPart.getStdErr(), "UTF16"), true);
		} catch (UnsupportedEncodingException e) {
			Activator.getInstance().logException("internal error", e);
			throw new RuntimeException("???", e);
		}
	}
	
	private static class InstanceHolder {
	      public static final ProjectEvaluatorFactory sInstance = new ProjectEvaluatorFactory();
	}
	
	public static ProjectEvaluatorFactory getInstance() {
		return InstanceHolder.sInstance;
	}
	
	public void clear() {
		reloaderForProject.clear();
		parserForProject.clear();
	}
	
	public void resetParser(IProject project) {
		parserForProject.remove(project);
		reloaderForProject.remove(project);
	}
	
	/**
	 * This method returns and shares a single evaluator for each project
	 */
	public Evaluator getEvaluator(IProject project) {
		Evaluator parser = parserForProject.get(project);
		
		if (parser == null) {
			parser = createProjectEvaluator(project);
			reloaderForProject.put(project, new ModuleReloader(parser));
			parserForProject.put(project, parser);
			return parser;
		}
		
		try {
			reloaderForProject.get(project).updateModules(new NullProgressMonitor());
		}
		catch (StaticError e) {
			// things may go wrong while reloading modules, simply because the modules still have parse errors in them.
			// these are safely ignored here, the user will have had feedback on those errors elsewhere
		}
		
		return parser;
	}

	/**
	 * This method creates a fresh evaluator every time you call it.
	 */
	public Evaluator createProjectEvaluator(IProject project) {
		Activator.getInstance().checkRascalRuntimePreconditions(project);
		GlobalEnvironment heap = new GlobalEnvironment();
		Evaluator parser = new Evaluator(ValueFactoryFactory.getValueFactory(), err, out, new ModuleEnvironment("***parser***", heap), heap);
		initializeProjectEvaluator(project, parser);
		return parser;
	}
	
	public Evaluator getBundleEvaluator(Bundle bundle) {
	  GlobalEnvironment heap = new GlobalEnvironment();
    Evaluator parser = new Evaluator(ValueFactoryFactory.getValueFactory(), err, out, new ModuleEnvironment("***parser***", heap), heap);
    initializeBundleEvaluator(bundle, parser);
    return parser;
	}

	

	/**
	 * This method configures an evaluator for use in an eclipse context. 
	 * @param project context to run the evaluator in, may be null
	 * @param evaluator the evaluator to configure, may not be null
	 */
	public void initializeProjectEvaluator(IProject project, Evaluator evaluator) {
		// NB. the code in this method is order dependent because it constructs a rascal module path in a particular order
		URIResolverRegistry resolverRegistry = evaluator.getResolverRegistry();
		
		ProjectURIResolver resolver = new ProjectURIResolver();
		resolverRegistry.registerInput(resolver);
		resolverRegistry.registerOutput(resolver);
		
		BundleURIResolver bundleResolver = new BundleURIResolver(resolverRegistry);
		resolverRegistry.registerInput(bundleResolver);
		resolverRegistry.registerOutput(bundleResolver);
		
		BootstrapURIResolver bootResolver = new BootstrapURIResolver();
		resolverRegistry.registerInputOutput(bootResolver);
		ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(resolverRegistry, "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
		resolverRegistry.registerInput(eclipseResolver);
		
		evaluator.addRascalSearchPath(URIUtil.rootScheme(eclipseResolver.scheme()));
		evaluator.addClassLoader(getClass().getClassLoader());
		
		configureRascalLibraryPlugins(evaluator);
		
		if (project != null) {
			try {
				addProjectToSearchPath(project, evaluator);
				
				IProject[] projects = project.getReferencedProjects();
				for (IProject ref : projects) {
					addProjectToSearchPath(ref, evaluator);
				}
			} 
			catch (URISyntaxException usex) {
				Activator.getInstance().logException("could not construct search path", usex);
			} 
			catch (CoreException e) {
				Activator.getInstance().logException("could not construct search path", e);
			}
			
			try {
				if (project.hasNature(JavaCore.NATURE_ID)) {
					configureRascalJavaPluginProject(project, evaluator); 
				}
				else {
					configureRawRascalProject(project, evaluator);
				}
				
				
			}
			catch (CoreException e) {
				Activator.getInstance().logException("exception while constructing classpath for evaluator", e);
			}
		}
		
	}

	 /**
   * This method configures an evaluator for use in an eclipse context. 
   * @param bundle context to run the evaluator in, may be null
   * @param evaluator the evaluator to configure, may not be null
   */
  public void initializeBundleEvaluator(Bundle bundle, Evaluator evaluator) {
    // NB. the code in this method is order dependent because it constructs a rascal module path in a particular order
    URIResolverRegistry resolverRegistry = evaluator.getResolverRegistry();
    
    ProjectURIResolver resolver = new ProjectURIResolver();
    resolverRegistry.registerInput(resolver);
    resolverRegistry.registerOutput(resolver);
    
    BundleURIResolver bundleResolver = new BundleURIResolver(resolverRegistry);
    resolverRegistry.registerInput(bundleResolver);
    resolverRegistry.registerOutput(bundleResolver);
    
    BootstrapURIResolver bootResolver = new BootstrapURIResolver();
    resolverRegistry.registerInputOutput(bootResolver);
    ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(resolverRegistry, "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
    resolverRegistry.registerInput(eclipseResolver);
    
    evaluator.addRascalSearchPath(URIUtil.rootScheme(eclipseResolver.scheme()));
    evaluator.addClassLoader(getClass().getClassLoader());
    
    configureRascalLibraryPlugins(evaluator);
    
    if (bundle != null) {
      try {
        addBundleToSearchPath(bundle, evaluator);
        evaluator.addClassLoader(new BundleClassLoader(bundle));
      } 
      catch (URISyntaxException e) {
        Activator.getInstance().logException("could not construct search path", e);
      }
    }
  }
	
	public void loadInstalledRascalLibraryPlugins() {
    IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
        .getExtensionPoint("rascal_eclipse", "rascalLibrary");

    if (extensionPoint == null) {
      return; // this may happen when nobody extends this point.
    }
    
    for (IExtension element : extensionPoint.getExtensions()) {
      String name = element.getContributor().getName();
      Bundle bundle = Platform.getBundle(name);
      GlobalEnvironment heap = new GlobalEnvironment();
      ModuleEnvironment env = new ModuleEnvironment("***" + name + "***", heap);
      Evaluator bundleEval = new Evaluator(ValueFactoryFactory.getValueFactory(), err, out, env, heap);
      URIResolverRegistry registry = bundleEval.getResolverRegistry();
      registry.registerInput(new BundleURIResolver(registry));
      ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(registry, "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
      registry.registerInput(eclipseResolver);
    
      bundleEval.addRascalSearchPath(URIUtil.rootScheme(eclipseResolver.scheme()));
      bundleEval.addClassLoader(getClass().getClassLoader());
      
      // first load the other plugins
      // TODO: support true dependencies
      configureRascalLibraryPlugins(bundleEval);
      
      // then run the main of the current one
      runLibraryPluginMain(bundleEval, bundle);
    } 
  }
	
	public static void configureRascalLibraryPlugins(Evaluator evaluator) {
	  IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
        .getExtensionPoint("rascal_eclipse", "rascalLibrary");

	  if (extensionPoint == null) {
	    return; // this may happen when nobody extends this point.
	  }
	  
	  try {
	    for (IExtension element : extensionPoint.getExtensions()) {
	      String name = element.getContributor().getName();
	      Bundle bundle = Platform.getBundle(name);
	      configureRascalLibraryPlugin(evaluator, bundle);
	    }
	  } 
	  catch (URISyntaxException e) {
	    Activator.log("could not load some library", e);
	  }
  }

  public static void configureRascalLibraryPlugin(Evaluator evaluator, Bundle bundle) throws URISyntaxException {
    List<String> roots = new RascalEclipseManifest().getSourceRoots(bundle);
    
    for (String root : roots) {
      evaluator.addRascalSearchPath(bundle.getResource(root).toURI());
    }
    
    evaluator.addClassLoader(new BundleClassLoader(bundle));
  }

  public static void runLibraryPluginMain(Evaluator evaluator, Bundle bundle) {
    try {
      RascalEclipseManifest mf = new RascalEclipseManifest();
      String mainModule = mf.getMainModule(bundle);
      evaluator.doImport(evaluator.getMonitor(), mainModule);
      evaluator.call(mf.getMainFunction(bundle));
    }
    catch (Throwable e) {
      Activator.log("Library defined by bundle " + bundle.getSymbolicName() + " has no main module or main function", e);
    }
  }

	/**
	 * This code is taken from http://wiki.eclipse.org/BundleProxyClassLoader_recipe
	 */
	private static class BundleClassLoader extends ClassLoader {
	  private Bundle bundle;
	  private ClassLoader parent;
	    
	  public BundleClassLoader(Bundle bundle) {
	    this.bundle = bundle;
	  }
	  
	  @Override
	  public Enumeration<URL> getResources(String name) throws IOException {
	    return bundle.getResources(name);
	  }
	  
	  @Override
	  public URL findResource(String name) {
	      return bundle.getResource(name);
	  }

	  @Override
	  public Class<?> findClass(String name) throws ClassNotFoundException {
	      return bundle.loadClass(name);
	  }
	  
	  @Override
	  public URL getResource(String name) {
	    return (parent == null) ? findResource(name) : super.getResource(name);
	  }

	  @Override
	  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
	    Class<?> clazz = (parent == null) ? findClass(name) : super.loadClass(name, false);
	    if (resolve)
	      super.resolveClass(clazz);
	    
	    return clazz;
	  }
	}
	
  public static void addProjectToSearchPath(IProject project, Evaluator parser)
			throws URISyntaxException {
    RascalEclipseManifest mf = new RascalEclipseManifest();
		for (String root : mf.getSourceRoots(project)) {
		  parser.addRascalSearchPath(URIUtil.create("project", project.getName(), "/" + root.trim()));
		}
	}
  
  public static void addBundleToSearchPath(Bundle project, Evaluator parser) throws URISyntaxException {
    List<String> srcs = new RascalEclipseManifest().getSourceRoots(project);
    
    if (srcs != null) {
      for (String root : srcs) {
        parser.addRascalSearchPath(project.getEntry(root.trim()).toURI());
      }
    }
    else {
      parser.addRascalSearchPath(project.getEntry("/").toURI());
    }
  }

	/**
	 * This code has to remain alive while there are still old-fashioned Rascal projects around that did not
	 * get configured with the Java nature.
	 * @deprecated
	 * @param project
	 * @param parser
	 */
	private void configureRawRascalProject(IProject project, Evaluator parser) {
		String projectBinFolder = "";
		
		if (project != null) {
			try {
				IResource res = project.findMember("bin");
				if (res != null) {
					projectBinFolder = res.getLocation().toString();
					URLClassLoader loader = new java.net.URLClassLoader(new URL[] {new URL("file", "",  projectBinFolder + "/")}, getClass().getClassLoader());
					parser.addClassLoader(loader);
				}
			} 
			catch (MalformedURLException e) {
				Activator.getInstance().logException("?? can not configure Rascal project due to malformed URL", e);
				return; 
			}
		}
		
		try {
			String rascalPlugin = jarForPlugin("rascal");
			String PDBValuesPlugin = jarForPlugin("org.eclipse.imp.pdb.values");

			Configuration.setRascalJavaClassPathProperty(
					rascalPlugin 
					+ File.pathSeparator 
					+ PDBValuesPlugin 
					+ File.pathSeparator 
					+ rascalPlugin 
					+ File.separator + "src" 
					+ File.pathSeparator 
					+ rascalPlugin + File.separator + "bin" 
					+ File.pathSeparator 
					+ PDBValuesPlugin + File.separator + "bin" 
					+ (projectBinFolder != "" ? File.pathSeparator + projectBinFolder : ""));
		} 
		catch (IOException e) {
			Activator.getInstance().logException("could not create classpath for parser compilation", e);
		}
	}

	private void collectClassPathForProject(IProject project, List<URL> classPath, List<String> compilerClassPath) {
		try {
			if (!project.hasNature(JavaCore.NATURE_ID)) {
				for (IProject ref : project.getReferencedProjects()) {
					collectClassPathForProject(ref, classPath, compilerClassPath);
				}
			}
			else {
				IJavaProject jProject = JavaCore.create(project);
				
				IPath binFolder = jProject.getOutputLocation();
				String binLoc = project.getLocation() + "/" + binFolder.removeFirstSegments(1).toString();
				compilerClassPath.add(binLoc);
				classPath.add(new URL("file", "",  binLoc + "/"));
				
				IClasspathEntry[] entries = jProject.getResolvedClasspath(true);
				
				for (int i = 0; i < entries.length; i++) {
					IClasspathEntry entry = entries[i];
					switch (entry.getEntryKind()) {
					case IClasspathEntry.CPE_LIBRARY:
						if (entry.getPath().segment(0).equals(project.getName())) {
							String file = project.getLocation() + "/" + entry.getPath().removeFirstSegments(1).toString();
							URL url = new URL("file", "", file);
							if (!classPath.contains(url)) {
								classPath.add(url);
								compilerClassPath.add(file);
							}
						}
						else {
							URL url = new URL("file", "", entry.getPath().toString());
							if (!classPath.contains(url)) {
								classPath.add(url);
								compilerClassPath.add(entry.getPath().toString());
							}
						}
						break;
					case IClasspathEntry.CPE_PROJECT:
						collectClassPathForProject((IProject) project.getWorkspace().getRoot().findMember(entry.getPath()), classPath, compilerClassPath);
						break;
					}
				}
			}
		}
		catch (CoreException e) {
			Activator.getInstance().logException("failed to configure classpath", e);
		} 
		catch (MalformedURLException e) {
			Activator.getInstance().logException("failed to configure classpath", e);
		}
	}
	
	private void configureRascalJavaPluginProject(IProject project,
			Evaluator parser) {
		List<URL> classPath = new LinkedList<URL>();
		List<String> compilerClassPath = new LinkedList<String>();
		
		collectClassPathForProject(project, classPath, compilerClassPath);

		// this registers the run-time path:
		System.err.println("Runtime classpath: " + classPath);
		URL[] urls = new URL[classPath.size()];
		classPath.toArray(urls);
		URLClassLoader classPathLoader = new URLClassLoader(urls, getClass().getClassLoader());
		parser.addClassLoader(classPathLoader);
		
		// this registers the compile-time path:
		String ccp = "";
		for (String elem : compilerClassPath) {
			ccp += File.pathSeparatorChar + elem;
		}
		
		System.err.println("Compiler CLASSPATH = " + ccp);
		Configuration.setRascalJavaClassPathProperty(ccp.substring(1));
	}

	private String jarForPlugin(String pluginName) throws IOException {
		URL rascalURI = FileLocator.resolve(Platform.getBundle(pluginName).getEntry("/"));
		
		try {
			if (rascalURI.getProtocol().equals("jar")) {
				String path = rascalURI.toURI().toASCIIString();
				return path.substring(path.indexOf("/"), path.indexOf('!'));
			}
			else {
				// TODO this is a monumental workaround, apparently the Rascal plugin gets unpacked and in 
				// it is a rascal.jar file that we should lookup...
				String path = rascalURI.getPath();
				File folder = new File(path);
				if (folder.isDirectory()) {
					File[] list = folder.listFiles();
					for (File f : list) {
						if (f.getName().startsWith(pluginName) && f.getName().endsWith(".jar")) {
							return f.getAbsolutePath();
						}
					}
				}
				
				return path;
			}
		}
		catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}
}
