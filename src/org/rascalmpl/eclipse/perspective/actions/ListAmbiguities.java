/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.perspective.actions;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.ambidexter.ReportView;

public class ListAmbiguities extends AbstractEditorAction {
	public ListAmbiguities(UniversalEditor editor) {
		super(editor, "List ambiguities");
		setImageDescriptor(Activator.getInstance().getImageRegistry().getDescriptor(IRascalResources.AMBIDEXTER));
	}
	
	@Override
	public void run() {
		if (editor != null && editor.isDirty()) {
			editor.doSave(new NullProgressMonitor());
		}
		
		// TODO create job for getting grammar
		
		String moduleName = getModuleName(project, file);
		
		try {
			ReportView part = (ReportView) PlatformUI.getWorkbench()
		    .getActiveWorkbenchWindow()
		    .getActivePage()
			.showView(ReportView.ID);
			 
			Object currentAst = editor.getParseController().getCurrentAst();
			
			if (currentAst != null) {
				part.list(project.getName(), moduleName, (IConstructor) currentAst);
			}
		} catch (PartInitException e) {
			RuntimePlugin.getInstance().logException("could not parse module", e);
		}
	}
	
	private String getModuleName(IProject project, IFile file) {
		String moduleName;
		
		IFolder srcFolder = project.getFolder(IRascalResources.RASCAL_SRC);
		if (srcFolder != null && srcFolder.exists()) {
			if (srcFolder.getProjectRelativePath().isPrefixOf(file.getProjectRelativePath())) {
				moduleName = file.getProjectRelativePath().removeFirstSegments(1).removeFileExtension().toPortableString();
				moduleName = moduleName.replaceAll(File.pathSeparator, "::");
				return moduleName;
			}
		}
		
		moduleName = file.getProjectRelativePath().removeFileExtension().toPortableString();
		moduleName = moduleName.replaceAll(File.pathSeparator, "::");
		return moduleName;
	}
}
