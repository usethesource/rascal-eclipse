/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.wizards;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (rsc).
 */

public class NewRascalFilePage extends WizardPage {
	private Text containerText;

	private Text fileText;

	private ISelection selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewRascalFilePage(ISelection selection) {
		super("wizardPage");
		setTitle("New Rascal Module");
		setDescription("This wizard creates a new Rascal module.");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Folder:");

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		label = new Label(container, SWT.NULL);
		label.setText("&Module name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.setFocus();
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			
			if (ssel.size() > 1) {
				return;
			}
			
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer) {
					container = (IContainer) obj;
				}
				else {
					container = ((IResource) obj).getParent();
				}
				
				containerText.setText(container.getFullPath().toString());
			}
			else if (obj instanceof IJavaProject) {
				containerText.setText(((IJavaProject) obj).getProject().getFullPath().toString());
			}
			else if (obj instanceof IPackageFragmentRoot) {
				containerText.setText(((IPackageFragmentRoot) obj).getPath().toString());
			}
			else if (obj instanceof IPackageFragment) {
				containerText.setText(((IPackageFragment) obj).getPath().toString());
			}
		}
		fileText.setText("");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new folder");
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()));
		String fileName = getFileName();

		if (getContainerName().length() == 0) {
			updateStatus("Folder must be specified");
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("Folder must exist");
			return;
		}
		
		List<String> sourceRoots = new RascalEclipseManifest().getSourceRoots(container.getProject());
		List<IResource> rootPaths = new LinkedList<>();
		for (String root : sourceRoots) {
		  rootPaths.add(container.getProject().findMember(root));
		}
		
		IResource runner = container;
		boolean found = false;

    stop: while (runner != null) {
      for (IResource root : rootPaths) {
        if (root.equals(runner)) {
          found = true;
          
          break stop;
        }
      }
      runner = runner.getParent();
    }
		
		String srcPath = rootPaths.get(0).getFullPath().toString();
    if (!found && !containerText.equals(srcPath)) {
		  containerText.setText(srcPath);
		}
		
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("Module name must be specified");
			return;
		}
		if (fileName.contains("::")) {
			updateStatus("Module name must not be qualified");
			return;
		}

		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			updateStatus("\".\" not allowed in module name.");
			return;
		}
		int slashLoc = fileName.lastIndexOf('\\');
		if (slashLoc != -1 && slashLoc != 0) {
			updateStatus("\"\\\" not allowed in module name.");
			return;
		}
		
		String rest = fileName.startsWith("\\") ? fileName.substring(1) : fileName;

		if (rest.length() == 0) {
			updateStatus("illegal module name");
			return;
		}
		
		if (!Character.isJavaIdentifierStart(rest.charAt(0))) {
			updateStatus("illegal module name");
			return;
		}
		
		for (int i = rest.length() - 1; i > 0; i--) {
			char c = rest.charAt(i);
			if (!(Character.isJavaIdentifierPart(c))) {
				updateStatus("illegal module name");
				return;
			}
		}
		
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getFileName() {
		return fileText.getText();
	}
}
