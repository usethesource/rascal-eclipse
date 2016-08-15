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
package org.rascalmpl.eclipse.views;

import static org.rascalmpl.eclipse.IRascalResources.ID_RASCAL_TUTOR_VIEW_PART;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.WorkbenchJob;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToErrorLog;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.tutor.RascalTutor;

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
				try {
					if (tutor != null) {
					  tutor.stop();
					}

					if (tutor == null) {
						monitor.beginTask("Loading Tutor server", 2);
						tutor = new RascalTutor();
						
						// to make sure we can find classes used in the Eclipse libraries.
						Evaluator eval = tutor.getRascalEvaluator();
						
						eval.addClassLoader(ProjectEvaluatorFactory.class.getClassLoader());
						
						// This is to make parser generation work (a JDK classpath has to be constructed)
						ProjectEvaluatorFactory.configureClassPath(Activator.getInstance().getBundle(), eval);						
						
						// if we go into edit mode and have some extra checks 
						if (tutor.isEditMode()) {
							IProject exampleProject = ResourcesPlugin.getWorkspace().getRoot().getProject("example-project");
							if (exampleProject == null || !exampleProject.exists() || !exampleProject.isOpen()) {
								throw new RuntimeException("Tutor will not start before you make sure example-project is open in your workspace. You can find it nested in the rascal project to import.");
							}
						}
						
						tutor.start(new RascalMonitor(monitor, new WarningsToErrorLog()));
					}
					
					monitor.worked(1);
					
					new WorkbenchJob("Loading tutor start page") {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							mainLocation = tutor.getServer().toString();
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
	}
}
