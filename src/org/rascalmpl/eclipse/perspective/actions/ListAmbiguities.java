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

import org.eclipse.core.resources.IFile;
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
import org.rascalmpl.eclipse.util.ResourcesToModules;

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
		
		listAmbiguities(editor, project, file);
	}

	public static void listAmbiguities(UniversalEditor editor, IProject project, IFile file) {
		String moduleName = ResourcesToModules.moduleFromFile(file);
		
		try {
			ReportView part = (ReportView) PlatformUI.getWorkbench()
		    .getActiveWorkbenchWindow()
		    .getActivePage()
			.showView(ReportView.ID);
			 
			Object currentAst = editor.getParseController().getCurrentAst();
			
			if (currentAst != null) {
				part.list(project.getName(), moduleName, (IConstructor) currentAst, new NullProgressMonitor());
			}
		} catch (PartInitException e) {
			RuntimePlugin.getInstance().logException("could not parse module", e);
		}
	}
}
