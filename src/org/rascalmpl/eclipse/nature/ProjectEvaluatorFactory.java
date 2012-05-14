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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.eclipse.console.internal.StdAndErrorViewPart;
import org.rascalmpl.eclipse.uri.BootstrapURIResolver;
import org.rascalmpl.eclipse.uri.BundleURIResolver;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.uri.ClassResourceInputOutput;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.values.ValueFactoryFactory;

public class ProjectEvaluatorFactory {
	
	private final WeakHashMap<IProject, Evaluator> parserForProject = new WeakHashMap<IProject, Evaluator>();
	private final WeakHashMap<IProject, ModuleReloader> reloaderForProject = new WeakHashMap<IProject, ModuleReloader>();
	private final PrintWriter out;
	
	private ProjectEvaluatorFactory() {
		try {
			out = new PrintWriter(new OutputStreamWriter(StdAndErrorViewPart.getStdOut(), "UTF16"));
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
		Evaluator parser = new Evaluator(ValueFactoryFactory.getValueFactory(), out, out, new ModuleEnvironment("***parser***", heap), heap);
		initializeProjectEvaluator(project, parser);
		return parser;
	}

	

	/**
	 * This method configures an evaluator for use in an eclipse context. 
	 * @param project context to run the evaluator in, may be null
	 * @param evaluator the evaluator to configure, may not be null
	 */
	public void initializeProjectEvaluator(IProject project, Evaluator parser) {
		// NB. the code in this method is order dependent because it constructs a rascal module path in a particular order
		URIResolverRegistry resolverRegistry = parser.getResolverRegistry();
		
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
		
		parser.addRascalSearchPath(URI.create(eclipseResolver.scheme() + ":///"));
		parser.addClassLoader(getClass().getClassLoader());
		
		if (project != null) {
			try {
				if (project.exists(new Path(IRascalResources.RASCAL_SRC))) {
					parser.addRascalSearchPath(new URI("project://" + project.getName() + "/" + IRascalResources.RASCAL_SRC));
				}
				else {
					parser.addRascalSearchPath(new URI("project://" + project.getName() + "/"));
				}
			} 
			catch (URISyntaxException usex) {
				throw new RuntimeException(usex);
			}
			
			try {
				if (project.hasNature(JavaCore.NATURE_ID)) {
					configureRascalJavaPluginProject(project, parser); 
				}
				else {
					configureRawRascalProject(project, parser);
				}
			}
			catch (CoreException e) {
				Activator.getInstance().logException("exception while constructing classpath for evaluator", e);
			}
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
