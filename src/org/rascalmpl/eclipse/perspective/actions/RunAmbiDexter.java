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
import java.net.URI;

import nl.cwi.sen1.AmbiDexter.AmbiDexterConfig;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.ambidexter.AmbiDexterRunner;
import org.rascalmpl.eclipse.ambidexter.AmbiDexterWizard;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.asserts.ImplementationError;

public class RunAmbiDexter implements IEditorActionDelegate {
	private UniversalEditor editor;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor instanceof UniversalEditor) {
			this.editor = (UniversalEditor) targetEditor;
		}
		else {
			this.editor = null;
		}
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void run(IAction action) {
		IProject project = editor.getParseController().getProject().getRawProject();
		IFile file = project.getFile(editor.getParseController().getPath());

		if (editor.isDirty()) {
			editor.doSave(new NullProgressMonitor());
		}
		
		// TODO create job for getting grammar
		
		Evaluator eval = ProjectEvaluatorFactory.getInstance().getEvaluator(project);
		String moduleName = getModuleName(project, file);
		IConstructor grammar = getGrammar(eval, moduleName);
		IRelation nestingRestr = eval.getNestingRestrictions(eval.getMonitor(), grammar);
		
		AmbiDexterWizard wizard = new AmbiDexterWizard(moduleName, grammar);
		WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);
		
		if (dialog.open() == WizardDialog.OK) {
			AmbiDexterConfig cfg = wizard.getConfig();
			cfg.filename = project.getName() + "/" + moduleName;
			AmbiDexterRunner.run(cfg, grammar, nestingRestr);
		}
	}

	private IConstructor getGrammar(Evaluator eval, String moduleName) {
		eval.doImport(eval.getMonitor(), moduleName);
		IConstructor grammar = eval.getExpandedGrammar(eval.getMonitor(), URI.create("rascal://" + moduleName));
		return grammar;
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
		
		throw new ImplementationError("could not compute modulename for " + file);
	}
}
