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
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.perspective;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.progress.UIJob;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.ambidexter.ReportView;
import org.rascalmpl.eclipse.console.internal.StdAndErrorViewPart;
import org.rascalmpl.eclipse.perspective.views.Tutor;

public class Factory implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();

		IFolderLayout folder = layout.createFolder("left", IPageLayout.LEFT, (float) 0.25, editorArea); 
		folder.addView(JavaUI.ID_PACKAGES);

		IFolderLayout replFolder = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.75, editorArea); 
		
		replFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		replFolder.addView(IPageLayout.ID_PROGRESS_VIEW);
		replFolder.addView(IPageLayout.ID_PROBLEM_VIEW);
		replFolder.addView(StdAndErrorViewPart.ID);
		replFolder.addView(Tutor.ID);
		
		IFolderLayout outlineFolder = layout.createFolder("outline", IPageLayout.RIGHT, (float) 0.75, editorArea);
		outlineFolder.addView(IPageLayout.ID_OUTLINE);
		outlineFolder.addView(ReportView.ID);
		
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);

		layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
		layout.addShowViewShortcut(JavaUI.ID_SOURCE_VIEW);
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		layout.addShowViewShortcut(StdAndErrorViewPart.ID);

		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_PROGRESS_VIEW);

		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
		layout.addNewWizardShortcut("rascal-eclipse.projectwizard");
		layout.addNewWizardShortcut("rascal_eclipse.wizards.NewRascalFile");
	}
	
	public static void launchConsole() {
		Job job = new UIJob("Launching console") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
					ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(IRascalResources.LAUNCHTYPE);
					ILaunchConfigurationWorkingCopy launch = type.newInstance(null, "Default Rascal Console");
					launch.setAttribute("loadPrelude", true);
					launch.launch(ILaunchManager.DEBUG_MODE, monitor);
				} catch (CoreException e) {
					Activator.getInstance().logException("could not start console", e);
				}
				
				return Status.OK_STATUS;
			}
		};
		
		job.setUser(true);
		job.schedule();
	}
}
