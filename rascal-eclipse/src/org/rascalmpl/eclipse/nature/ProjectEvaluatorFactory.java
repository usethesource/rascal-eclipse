/*******************************************************************************
 * Copyright (c) 2009-2015 CWI
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
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IProject;
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
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;
import org.rascalmpl.interpreter.ConsoleRascalMonitor;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.RascalSearchPath;
import org.rascalmpl.interpreter.utils.RascalManifest;
import org.rascalmpl.uri.ILogicalSourceLocationResolver;
import org.rascalmpl.uri.ProjectURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.impulse.runtime.RuntimePlugin;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.exceptions.FactTypeUseException;
import io.usethesource.vallang.io.StandardTextReader;

public class ProjectEvaluatorFactory {
	
	private final WeakHashMap<IProject, Evaluator> parserForProject = new WeakHashMap<IProject, Evaluator>();
	private final WeakHashMap<IProject, ModuleReloader> reloaderForProject = new WeakHashMap<IProject, ModuleReloader>();
	private final PrintWriter out;
	private final PrintWriter err;
	
	private ProjectEvaluatorFactory() {
		out = new PrintWriter(RuntimePlugin.getInstance().getConsoleStream());
		err = new PrintWriter(RuntimePlugin.getInstance().getConsoleStream(), true);
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
	
	public RascalSearchPath getProjectSearchPath(IProject project) {
		Evaluator eval = getOrCreateEvaluator(project);
		return eval.getRascalResolver();
	}
	
	public Evaluator getEvaluator(IProject project) {
		Evaluator parser = getOrCreateEvaluator(project);
		assert reloaderForProject.get(project) != null;
		reloaderForProject.get(project).updateModules(new NullProgressMonitor(), new WarningsToPrintWriter(parser.getStdErr()), Collections.emptySet());
		return parser;
	}
	/**
	 * This method returns and shares a single evaluator for each project
	 */
	public Evaluator getEvaluator(IProject project, IWarningHandler warnings) {
		Evaluator parser = getOrCreateEvaluator(project);
		assert reloaderForProject.get(project) != null;
		reloaderForProject.get(project).updateModules(new NullProgressMonitor(), warnings, Collections.emptySet());
		return parser;
	}
	
	private static boolean isRascalBootstrapProject(IProject project) {
        return "rascal".equals(project.getName());
    }
	
	public void reloadProject(IProject project, IWarningHandler handler, Set<String> ignored) {
		ModuleReloader reloader = reloaderForProject.get(project);
		
		if (reloader != null) {
			reloader.updateModules(new NullProgressMonitor(), handler, ignored);
		}
	}

	private Evaluator getOrCreateEvaluator(IProject project) {
		Evaluator parser = parserForProject.get(project);
		
		if (parser == null) {
			parser = createProjectEvaluator(project, err, out);
			reloaderForProject.put(project, new ModuleReloader(project, parser, new WarningsToPrintWriter(parser.getStdErr())));
			parserForProject.put(project, parser);
		}
		
		return parser;
	}

	/**
	 * This method creates a fresh evaluator every time you call it.
	 */
	public Evaluator createProjectEvaluator(IProject project, Writer err, Writer out) {
		Activator.getInstance().checkRascalRuntimePreconditions(project);
		GlobalEnvironment heap = new GlobalEnvironment();
		Evaluator parser = new Evaluator(ValueFactoryFactory.getValueFactory(), new PrintWriter(err), new PrintWriter(out), new ModuleEnvironment("$root$", heap), heap);
		configure(project, parser);
		return parser;
	}
	
	public Evaluator getBundleEvaluator(Bundle bundle) {
	    GlobalEnvironment heap = new GlobalEnvironment();
	    Evaluator parser = new Evaluator(ValueFactoryFactory.getValueFactory(), err, out, new ModuleEnvironment("$parser$", heap), heap);
	    initializeBundleEvaluator(bundle, parser);
	    return parser;
	}

	public Evaluator getBundleEvaluator(Bundle bundle, Writer err, Writer out) {
	    GlobalEnvironment heap = new GlobalEnvironment();
	    Evaluator parser = new Evaluator(ValueFactoryFactory.getValueFactory(), new PrintWriter(err), new PrintWriter(out), new ModuleEnvironment("$parser$", heap), heap);
	    initializeBundleEvaluator(bundle, parser);
	    return parser;
	}
	
	/**
	 * This method configures an evaluator for use in an eclipse context
	 */
	public static void configure(Evaluator evaluator, IProject project) {
		// make sure errors show up somewhere
		evaluator.setMonitor(new ConsoleRascalMonitor(RuntimePlugin.getInstance().getConsoleStream()));
		// NB. the code in this method is order dependent because it constructs a rascal module path in a particular order
	    evaluator.addRascalSearchPath(URIUtil.rootLocation("test-modules"));
		evaluator.addClassLoader(ProjectEvaluatorFactory.class.getClassLoader());
		
		if (project == null || !isRascalBootstrapProject(project)) {
		    evaluator.addClassLoader(Evaluator.class.getClassLoader());
		    evaluator.addRascalSearchPath(URIUtil.rootLocation("std")); 
		    // add where the sources of the eclipse dependant standard lib are in its jar:

		    addBundleToSearchPath(Platform.getBundle(IRascalResources.ID_RASCAL_ECLIPSE_PLUGIN), evaluator);
		}
	}
	
	/**
	 * This method configures an evaluator for use in an eclipse context. 
	 * @param project context to run the evaluator in, may be null
	 * @param evaluator the evaluator to configure, may not be null
	 */
	public void configure(IProject project, Evaluator evaluator) {
		if (project != null) {
			try {
				addProjectToSearchPath(project, evaluator);
			} 
			catch (URISyntaxException usex) {
				Activator.getInstance().logException("could not construct search path", usex);
			} 
		}
		
		configure(evaluator, project);

		try {
			configureClassPath(project, evaluator); 
		}
		catch (CoreException e) {
		  Activator.getInstance().logException("exception while constructing classpath for evaluator", e);
		}
	}
	
	private static void configure(Bundle bundle, Evaluator evaluator) {
		configure(evaluator, null);
		configure(bundle, evaluator, new HashSet<String>());
	}
	
	private static void configure(Bundle bundle, Evaluator eval, Set<String> configured) {
		if (bundle != null) {
			configured.add(bundle.getSymbolicName());

			try {
				addBundleToSearchPath(bundle, eval);
				eval.addClassLoader(new BundleClassLoader(bundle));
				configureClassPath(bundle, eval);

				RascalEclipseManifest mf = new RascalEclipseManifest();
				
				List<String> libs = mf.getRequiredLibraries(bundle);
				if (libs != null) {
					for (String required : libs) {
					    if (required.startsWith("|")) {
					        eval.addRascalSearchPath((ISourceLocation) new StandardTextReader().read(eval.getValueFactory(), new StringReader(required)));
					    }
					    else {
					        URI entryURI = URIUtil.fromURL(bundle.getEntry(required));
					        addJarToSearchPath(eval.getValueFactory().sourceLocation(entryURI), eval);
					    }
					}
				}
			} 
			catch (URISyntaxException | FactTypeUseException | IOException e) {
				Activator.getInstance().logException("could not construct search path", e);
			}
		}
	}

	 /**
   * This method configures an evaluator for use in an eclipse context. 
   * @param bundle context to run the evaluator in, may be null
   * @param evaluator the evaluator to configure, may not be null
   */
  public void initializeBundleEvaluator(Bundle bundle, Evaluator evaluator) {
    configure(bundle, evaluator);
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
          Evaluator bundleEval = getBundleEvaluator(bundle);

          // then run the main of the current one
          runLibraryPluginMain(bundleEval, bundle);
      } 
  }
	
  public static void runLibraryPluginMain(Evaluator evaluator, Bundle bundle) {
    try {
      RascalEclipseManifest mf = new RascalEclipseManifest();
      
      if (!mf.hasManifest(bundle)) {
        return;
      }
      
      String mainModule = mf.getMainModule(bundle);
      String mainFunction = mf.getMainFunction(bundle);
      
      // we only run a function if the main module and function have been configured.
      // this is to give the option to NOT run a main module, but provide only the 
      // plugin as a library to other plugins.
      if (mainModule != null && mainFunction != null) {
    	evaluator.getStdOut().println("Loading module " + mainModule + " and calling " + mainFunction);
    	evaluator.getStdOut().flush();
        evaluator.doImport(evaluator.getMonitor(), mainModule);
        evaluator.call(mainFunction);
      }
    }
    catch (Throwable e) {
      Activator.log("Library defined by bundle " + bundle.getSymbolicName() + " has no main module or main function", e);
    }
  }

	public static void addProjectToSearchPath(IProject project, Evaluator eval)
			throws URISyntaxException {
		RascalEclipseManifest mf = new RascalEclipseManifest();
		for (String root : mf.getSourceRoots(project)) {
			eval.addRascalSearchPath(ProjectURIResolver.constructProjectURI(project, project.getFile(root).getProjectRelativePath()));
		}
		
		List<String> requiredLibraries = mf.getRequiredLibraries(project);
		if (requiredLibraries != null) {
			for (String lib : requiredLibraries) {
			    try {
			        if (lib.startsWith("|")) {
			            ISourceLocation library = (ISourceLocation) new StandardTextReader().read(eval.getValueFactory(), new StringReader(lib));
			            ISourceLocation projectLib = URIUtil.changeScheme(library, "project");
			            
			            if (URIResolverRegistry.getInstance().exists(projectLib)) {
			                // we give precedence to the project dependency over the installed library dependency
			                for (String root : mf.getSourceRoots(projectLib)) {
			                    eval.addRascalSearchPath(URIUtil.getChildLocation(projectLib, root));
			                }
			            }
			            else {
			                // otherwise we expect to find the sources in a library, at the root of the jar
			                eval.addRascalSearchPath(library);
			            }
			        }
			        else {
			            addJarToSearchPath(ProjectURIResolver.constructProjectURI(project, project.getFile(lib).getProjectRelativePath()), eval);
			        }
			    } catch (FactTypeUseException | IOException e) {
			        Activator.log(e.getMessage(), e);
                }
			}
		}
		
		/** So that the target folder of a project can serve as a lib URI during development time in Eclipse: */
		URIResolverRegistry.getInstance().registerLogical(new ILogicalSourceLocationResolver() {
            @Override
            public String scheme() {
                return "lib";
            }
            
            @Override
            public ISourceLocation resolve(ISourceLocation input) {
                if (input.getAuthority().equals(authority())) {
                    ISourceLocation root = URIUtil.correctLocation("project", authority(), "bin");

                    if (project.getFile("target").exists()) {
                        root = URIUtil.correctLocation("project", authority(), "target/classes");
                    }

                    return URIUtil.getChildLocation(root, input.getPath());
                }
                else {
                    return input; // not this project, let the lib:/// resolver take care of it.
                }
            }
            
            @Override
            public String authority() {
                return project.getName();
            }
        });
	}
  
  public static void addJarToSearchPath(ISourceLocation jar, Evaluator eval) {
      try {
          String scheme = jar.getScheme().equals("file") ? "jar" :  "jar+" + jar.getScheme();
          String path = jar.getPath().endsWith("!/") ? jar.getPath() : jar.getPath() + "!/";
          ISourceLocation prefix = URIUtil.changeScheme(URIUtil.changePath(jar, path), scheme);
          
          RascalManifest mf = new RascalManifest();
          List<String> roots = mf.getManifestSourceRoots(mf.manifest(jar));

          if (roots != null) {
              for (String root : roots) {
                  eval.addRascalSearchPath(URIUtil.getChildLocation(prefix, root));
              }
          }
      } catch (URISyntaxException e) {
        Activator.log("could not add jar to search path " + jar, e);
      } 
  }
  
  public static void addBundleToSearchPath(Bundle bundle, Evaluator eval) {
	  RascalEclipseManifest mf = new RascalEclipseManifest();
	  List<String> srcs = mf.getSourceRoots(bundle);

	  if (srcs != null) {
		  for (String root : srcs) {
			  eval.addRascalSearchPath(URIUtil.correctLocation("plugin", bundle.getSymbolicName(), "/" + root.trim()));
		  }
	  }
	  else {
		  eval.addRascalSearchPath(URIUtil.correctLocation("plugin", bundle.getSymbolicName(), "/"));
	  }
  }

  private static void collectClassPathForBundle(Bundle bundle, List<URL> classPath, List<String> compilerClassPath) {
    try {
      File file = FileLocator.getBundleFile(bundle);
      
      // in the case we are loading a bundle which is actually a first Eclipse level source project,
      // we now just concatenate the bin folder and hope to find our class files there.
      // this is only relevant in the context of people developing Rascal, not people using it.
      if (file.isDirectory()) {
        File bin = new File(file, "bin");
        if (bin.exists()) {
          file = bin;
        }
      }
      
      URL url = file.toURI().toURL();
      
      if (classPath.contains(url)) {
        return; // kill infinite loop
      }

      
      classPath.add(0, url);
      compilerClassPath.add(0, file.getAbsolutePath());

      BundleWiring wiring = bundle.adapt(BundleWiring.class);

      for (BundleWire dep : wiring.getRequiredWires(null)) {
        collectClassPathForBundle(dep.getProviderWiring().getBundle(), classPath, compilerClassPath);
      }
    } 
    catch (IOException e) {
     Activator.log("error construction classpath for bundle: " + bundle.getSymbolicName(), e);
    } 
  }
  
	private void collectClassPathForProject(IProject project, List<URL> classPath, List<String> compilerClassPath, Evaluator parser) {
	    if (project == null) {
	        return;
	    }
	    
		try {
		    IJavaProject jProject = JavaCore.create(project);

		    IPath binFolder = jProject.getOutputLocation();
		    String binLoc = project.getLocation() + "/" + binFolder.removeFirstSegments(1).toString();
		    compilerClassPath.add(binLoc);

		    URL binURL = new URL("file", "",  binLoc + "/");
		    parser.addClassLoader(new URLClassLoader(new URL[] {binURL}, getClass().getClassLoader()));
		    classPath.add(binURL);

		    if (!jProject.isOpen()) {
		        return;
		    }
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
		            collectClassPathForProject((IProject) project.getWorkspace().getRoot().findMember(entry.getPath()), classPath, compilerClassPath, parser);
		            break;
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
	
	public void configureClassPath(IProject project, Evaluator parser) throws CoreException {
		List<URL> classPath = new LinkedList<URL>();
		List<String> compilerClassPath = new LinkedList<String>();
		Bundle rascalBundle = Activator.getInstance().getBundle();
		
		// order is important
		if (project != null && project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
			collectClassPathForProject(project, classPath, compilerClassPath, parser);
		}
		
		collectClassPathForBundle(rascalBundle, classPath, compilerClassPath);
		configureClassPath(parser, classPath, compilerClassPath);
	}
	
	public static void configureClassPath(Bundle bundle, Evaluator evaluator) {
	  List<URL> classPath = new LinkedList<URL>();
	  List<String> compilerClassPath = new LinkedList<String>();
	  collectClassPathForBundle(bundle, classPath, compilerClassPath);
	  Bundle rascalBundle = Activator.getInstance().getBundle();
	  if (!bundle.getSymbolicName().equals(rascalBundle.getSymbolicName())) {
		  collectClassPathForBundle(rascalBundle, classPath, compilerClassPath);
	  }
	  configureClassPath(evaluator, classPath, compilerClassPath);
	}
	
	private static void configureClassPath(Evaluator parser, List<URL> classPath, List<String> compilerClassPath) {
        // this registers the run-time path:
        URL[] urls = new URL[classPath.size()];
        classPath.toArray(urls);
        URLClassLoader classPathLoader = new URLClassLoader(urls, ProjectEvaluatorFactory.class.getClassLoader());
        parser.addClassLoader(classPathLoader);
        
        try {
            // The Java compiler does not extract classes from nested jars, therefore we try to find a file URL for the nested fat
            // jar (probably extracted in a temp folder by OSGI) and add it to the Java compiler classpath which is used for compiling
            // generated code by the Rascal parser generator:
            Bundle rascalBundle = Activator.getInstance().getBundle();
        	URL entry = FileLocator.toFileURL(rascalBundle.getEntry("lib/rascal.jar"));
        
			// this registers the compile-time path:
            String ccp = new File(URIUtil.fromURL(entry)).getAbsolutePath();
            for (String elem : compilerClassPath) {
                ccp += File.pathSeparatorChar + elem;
            }

            parser.getConfiguration().setRascalJavaClassPathProperty(ccp);
        } catch (URISyntaxException e) {
            Activator.log("URL of rascal is not a valid URI???", e);
        } catch (IOException e1) {
            Activator.log("could not find fat rascal jar", e1);
		}
    }
}

