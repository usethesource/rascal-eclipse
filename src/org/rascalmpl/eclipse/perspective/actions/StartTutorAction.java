/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.perspective.actions;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.uri.BundleURIResolver;
import org.rascalmpl.interpreter.Configuration;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.library.experiments.RascalTutor.RascalTutor;
import org.rascalmpl.uri.ClassResourceInputOutput;
import org.rascalmpl.uri.URIResolverRegistry;


public class StartTutorAction extends Job implements IWorkbenchWindowActionDelegate {
	private static RascalTutor tutor;
	private static Object lock = new Integer(42);
    
	public StartTutorAction() {
		super("Tutor");
	}
	
	public static void stopTutor() {
		stop();
	}
	
	public void dispose() {
		stop();
	}
	
	private static void stop() {
		if (tutor != null) {
			try {
				tutor.stop();
				tutor = null;
			} catch (Exception e) {
				Activator.getInstance().logException("could not stop tutor", e);
			}
		}
	}

	public void init(IWorkbenchWindow window) {
		// do nothing
	}

	public void run(IAction action) {
		schedule();
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
	
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		synchronized (lock) {
			int port = 9000;
			try {
				stop();

				if (tutor == null) {
					monitor.beginTask("Loading Tutor server", 2);
					tutor = new RascalTutor();
					URIResolverRegistry registry = tutor.getResolverRegistry();
					BundleURIResolver resolver = new BundleURIResolver(registry);
					registry.registerInput(resolver);
					registry.registerOutput(resolver);

					Evaluator eval = tutor.getRascalEvaluator();
					ClassResourceInputOutput eclipseResolver = new ClassResourceInputOutput(eval.getResolverRegistry(), "eclipse-std", RascalScriptInterpreter.class, "/org/rascalmpl/eclipse/library");
					eval.getResolverRegistry().registerInput(eclipseResolver);
					eval.addRascalSearchPath(URI.create(eclipseResolver.scheme() + ":///"));
					eval.addClassLoader(getClass().getClassLoader());

					String rascalPlugin = jarForPlugin("rascal");
					String rascalEclipsePlugin = jarForPlugin("rascal_eclipse");
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
							+ File.pathSeparator
							+ rascalEclipsePlugin
							+ File.pathSeparator
							+ rascalEclipsePlugin + File.separator + "bin"
							);

					for (int i = 0; i < 100; i++) {
						try {
							tutor.start(port, new RascalMonitor(monitor));
							break;
						}
						catch (BindException e) {
							port += 1;
						}
					}
				}
				
				monitor.worked(1);

				final int foundPort = port;
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							int style = IWorkbenchBrowserSupport.AS_EDITOR 
									| IWorkbenchBrowserSupport.LOCATION_BAR 
									| IWorkbenchBrowserSupport.STATUS
									;
							monitor.worked(2);
							IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(style, "RascalTutor", "Rascal Tutor", "Rascal Tutor");
							browser.openURL(new URL("http://127.0.0.1:" + foundPort));
							monitor.done();
						} catch (PartInitException e) {
							Activator.getInstance().logException("Could not start browser for tutor", e);
						} catch (MalformedURLException e) {
							Activator.getInstance().logException("Could not start browser for tutor", e);
						}
					}
				});

			} 
			catch (Throwable e) {
				Activator.getInstance().logException("Could not start tutor server", e);
			}
		}
		
		return Status.OK_STATUS;
	}
}
