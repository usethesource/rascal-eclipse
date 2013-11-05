/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Various members of the Software Analysis and Transformation Group - CWI
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package org.rascalmpl.eclipse.perspective.views;

import static org.rascalmpl.eclipse.IRascalResources.ID_RASCAL_TUTOR_VIEW_PART;

import java.io.IOException;
import java.net.BindException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.WorkbenchJob;
import org.osgi.framework.Bundle;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToPrintWriter;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.tutor.RascalTutor;
import org.rascalmpl.uri.ClassResourceInput;
import org.rascalmpl.uri.FileURIResolver;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;

public class Tutor extends ViewPart {
	public static final String ID = ID_RASCAL_TUTOR_VIEW_PART;
	
	private Browser browser;
	private volatile String mainLocation;
    private RascalTutor tutor;
	private Object lock = new Object();

	private ExecutorService backgroundTasks;
    
	public Tutor() { 
		backgroundTasks = Executors.newSingleThreadExecutor(); 
	}
	
	public void gotoPage(final String page) {
		if (mainLocation == null) {
			// lets wait in the background for the tutor being loaded, we know it will at some point..
			backgroundTasks.execute(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(10);
						gotoPage(page);
					} catch (InterruptedException e) {
					}
				}
			});
		}
		else {
			new WorkbenchJob("Loading concept page") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (mainLocation == null) {
						// this shouldn't happen but lets just be sure
						gotoPage(page);
					}
					else {
						browser.setUrl(mainLocation + page);
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		browser.setText("<html><body>The Rascal tutor is now loading: <progress max=\"100\"></progress></body></html>");
		new StarterJob().schedule();
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}
	
	@Override
	public void dispose() {
		stop();
	}
	
	private void stop() {
		if (tutor != null) {
			try {
				tutor.stop();
				tutor = null;
			} catch (Exception e) {
				Activator.log("could not stop tutor", e);
			}
		}
	}
	
	private class StarterJob extends Job {

		public StarterJob() {
			super("Starting tutor");
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			synchronized (lock) {
				int port = 9000;
				try {
					stop();

					if (tutor == null) {
						monitor.beginTask("Loading Tutor server", 2);
						tutor = new RascalTutor();
						
						Bundle bundle = Activator.getInstance().getBundle();
						Evaluator eval = tutor.getRascalEvaluator();
						
						// somehow the tutor should get access to the library files in this project.
						// we create a new scheme especially for this, which is used in the mapping file: remote-concepts.value
            ProjectEvaluatorFactory.getInstance().initializeBundleEvaluator(bundle, eval);
						URIResolverRegistry registry = eval.getResolverRegistry();
						
						for (final String lib : new String[] { "rascal", "rascal-eclipse" }) {
						  final String courseSrc = System.getProperty("rascal.courses.lib." + lib);
					  
						  if (courseSrc != null) {
						    FileURIResolver fileURIResolver = new FileURIResolver() {
						      @Override
						      public String scheme() {
						        return "clib-" + lib;
						      }

						      @Override
						      protected String getPath(URI uri) {
						        String path = uri.getPath();
						        return courseSrc + (path.startsWith("/") ? path : ("/" + path));
						      }
						    };
					      
					      registry.registerInputOutput(fileURIResolver);
					      eval.addRascalSearchPath(URIUtil.rootScheme("clib-" + lib));
					    }
						}
			      
						for (int i = 0; i < 100; i++) {
							try {
								tutor.start(port, new RascalMonitor(monitor, new WarningsToPrintWriter(eval.getStdErr())));
								break;
							}
							catch (BindException e) {
								port += 1;
							}
						}
					}
					
					monitor.worked(1);
					
					final int foundPort = port;
					new WorkbenchJob("Loading tutor start page") {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							mainLocation = "http://127.0.0.1:" + foundPort;
							browser.setUrl(mainLocation);
							return Status.OK_STATUS;
						}
					}.schedule();
					
				}
				catch (Throwable e) {
					Activator.getInstance().logException("Could not start tutor server", e);
				}
			}
			
			return Status.OK_STATUS;
		}
		
		private String jarForPlugin(String pluginName) throws IOException {
			URL rascalURI = FileLocator.resolve(Platform.getBundle(pluginName).getEntry("/"));
			
			try {
				if (rascalURI.getProtocol().equals("jar")) {
					/*
					 * Installed plug-in as jar file. E.g. URI has form
					 * jar:file:/path/to/eclipse/plugins/rascal_0.5.2.201210301241.jar!/		
					 */
					String path = null;
					try {
						path = rascalURI.toURI().toASCIIString();
					}
					catch (URISyntaxException e) {
						// okay so the path contains special chars, lets be smarter
						// and try one possible way of creating a path
						path = rascalURI.getPath();
						if (path.startsWith("file:")) {
							path = path.substring(5);
						}
						path = URIUtil.create(rascalURI.getProtocol(), null, path).toString();
						
					}
					return path.substring(path.indexOf("/"), path.indexOf('!'));
				
				} else {					
					/*
					 * I.e. Rascal is launched in second level and path is 
					 * pointing to first level source folder.
					 */
					String path = rascalURI.getPath();					
					return path;
				}
			}
			catch (URISyntaxException e) {
				throw new IOException(e);
			}
		}
	}
}
