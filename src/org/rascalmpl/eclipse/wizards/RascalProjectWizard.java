/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
*******************************************************************************/
package org.rascalmpl.eclipse.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.pde.core.project.IBundleProjectDescription;
import org.eclipse.pde.core.project.IBundleProjectService;
import org.eclipse.pde.core.project.IRequiredBundleDescription;
import org.eclipse.pde.internal.core.PDEClasspathContainer;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.natures.PDE;
import org.eclipse.pde.internal.core.project.RequiredBundleDescription;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;


@SuppressWarnings("restriction")
public class RascalProjectWizard extends BasicNewProjectResourceWizard {

	
	@SuppressWarnings("restriction")
	@Override
	public boolean performFinish() {
		if (!super.performFinish()) {
			return false;
		}
		
		final IProject project = getNewProject();
		
		// this code initializes the project to be a PDE plugin project as well as a java project and
		// puts the right classpath entries and plugin dependencies such that building and running will work.
		IRunnableWithProgress job = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException,
					InterruptedException {
				try {
					BundleContext context = Activator.getInstance().getBundle().getBundleContext();
					ServiceReference<IBundleProjectService> ref = context.getServiceReference(IBundleProjectService.class);
					try {
						IBundleProjectService service = context.getService(ref);
						IBundleProjectDescription plugin = service.getDescription(project);
						plugin.setBundleName(project.getName());
						plugin.setSymbolicName(project.getName().replaceAll("-", "_"));
						plugin.setNatureIds(new String[] { IRascalResources.ID_RASCAL_NATURE, JavaCore.NATURE_ID, IBundleProjectDescription.PLUGIN_NATURE});
						plugin.setRequiredBundles(new IRequiredBundleDescription[] { 
								new RequiredBundleDescription("org.eclipse.imp.pdb.values", null, false, false),
								new RequiredBundleDescription("rascal", null, false, false)});
						
						IProjectDescription description = project.getDescription();
						description.setBuildConfigs(new String[] { "org.eclipse.jdt.core.javabuilder", "org.eclipse.pde.ManifestBuilder", "org.eclipse.pde.SchemaBuilder" });
						project.setDescription(description, monitor);
						plugin.apply(monitor);

						IJavaProject jProject = JavaCore.create(project);
						IClasspathEntry[] oldClasspath = jProject.getRawClasspath();
						IClasspathEntry[] newClasspath = new IClasspathEntry[oldClasspath.length + 2];
						System.arraycopy(oldClasspath, 0, newClasspath, 2, oldClasspath.length);
						newClasspath[0] = JavaRuntime.getDefaultJREContainerEntry();
						newClasspath[1] = JavaCore.newContainerEntry(new Path("org.eclipse.pde.core.requiredPlugins"));
//						IFolder folder = project.getFolder("src");
//						if (!folder.exists()) {
//							folder.create(true, true, monitor);
//						}
//						newClasspath[2] = JavaCore.newSourceEntry(folder.getProjectRelativePath().makeAbsolute());
						jProject.setRawClasspath(newClasspath, monitor);
						jProject.save(monitor, false);
					}
					finally {
						context.ungetService(ref);
					}
				} catch (CoreException e) {
					Activator.getInstance().logException("could not initialize Rascal project", e);
					throw new InterruptedException();
				}
			}
		};

		if (project != null) {
			try {
				getContainer().run(true, true, job);
			} catch (InvocationTargetException e) {
				Activator.getInstance().logException("could not initialize new Rascal project", e);
				return false;
			} catch (InterruptedException e) {
				return false;
			}
			return true;
		}
		
		return false;
	}


}
