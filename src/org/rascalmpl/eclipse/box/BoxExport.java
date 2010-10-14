/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.rascalmpl.eclipse.box;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardExportResourcesPage;

/**
 * Wizard for exporting resources from the workspace to a Java Archive (JAR) file.
 * &lt;p&gt;
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * &lt;/p&gt;
 * &lt;p&gt;
 * Example:
 * &lt;pre&gt;
 * IWizard wizard = new JarPackageWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * &lt;/pre&gt;
 * During the call to &lt;code&gt;open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, the user-selected workspace resources 
 * are exported to the user-specified zip file, the dialog closes, and the call
 * to &lt;code&gt;open</code> returns.
 * &lt;/p&gt;
 */
public class BoxExport extends Wizard implements IExportWizard {


	private WizardExportResourcesPage fExportPage;

	private IStructuredSelection fSelection;
	
	protected String cmd = "toHtml";

	/**
	 * Creates a wizard for exporting workspace resources to a JAR file.
	 */
	public BoxExport() {
	}
	

	/**
	 * {@inheritDoc}
	 */
	public void addPages() {
		super.addPages();
		fExportPage = new BoxExportPage(cmd, StructuredSelection.EMPTY);
		addPage(fExportPage);
	}


	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		IWizardPage[] pages= getPages();
		for (int i= 0; i < getPageCount(); i++) {
			IWizardPage page= pages[i];
			if (page instanceof BoxExportPage) {
				BoxExportPage boxExportPage = (BoxExportPage) page;
				boxExportPage.finish();
			}
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		// setWindowTitle("HELP:");
		// System.err.println("HELP init:"+this.getDialogSettings());
		// System.err.println("HELP init:"+this.getDialogSettings().getName());
	}

}
