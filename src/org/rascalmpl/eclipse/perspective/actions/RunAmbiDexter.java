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
import java.util.regex.Pattern;

import nl.cwi.sen1.AmbiDexter.AmbiDexterConfig;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.ambidexter.AmbiDexterRunner;
import org.rascalmpl.eclipse.ambidexter.AmbiDexterWizard;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.asserts.ImplementationError;

public class RunAmbiDexter extends AbstractEditorAction {
	
	public RunAmbiDexter(UniversalEditor editor) {
		super(editor, "Run AmbiDexter");
		setImageDescriptor(Activator.getInstance().getImageRegistry().getDescriptor(IRascalResources.AMBIDEXTER));
	}
	
	@Override
	public void run() {
		if (editor.isDirty()) {
			editor.doSave(new NullProgressMonitor());
		}
		
		class GrammarJob extends Job {
			
			public GrammarJob() {
				super("Preparing AmbiDexter");
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Retrieving Rascal Grammar", 4);
					Evaluator eval = ProjectEvaluatorFactory.getInstance().getEvaluator(project);
					monitor.worked(1);
					final String moduleName = getModuleName(project, file);
					monitor.worked(1);
					final IConstructor grammar = getGrammar(eval, moduleName);
					monitor.worked(1);
					final IRelation nestingRestr = eval.getNestingRestrictions(eval.getMonitor(), grammar);				
					monitor.worked(1);
					monitor.done();
					
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							AmbiDexterWizard wizard = new AmbiDexterWizard(moduleName, grammar);
							WizardDialog dialog = new WizardDialog(editor.getSite().getShell(), wizard);

							if (dialog.open() == WizardDialog.OK) {
								AmbiDexterConfig cfg = wizard.getConfig();
								cfg.filename = project.getName() + "/" + moduleName;
								AmbiDexterRunner.run(cfg, grammar, nestingRestr);
							}
						}
					});
					
					return Status.OK_STATUS;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return Status.CANCEL_STATUS;
			}
		};
		
		GrammarJob j = new GrammarJob();
		j.setUser(true);
		j.schedule();
	}
	
	private static IConstructor getGrammar(Evaluator eval, String moduleName) {
		eval.doImport(eval.getMonitor(), moduleName);
		IConstructor grammar = eval.getExpandedGrammar(eval.getMonitor(), URI.create("rascal://" + moduleName));
		return grammar;
	}

	private static String getModuleName(IProject project, IFile file) {
		String moduleName;
		
		IFolder srcFolder = project.getFolder(IRascalResources.RASCAL_SRC);
		
		if (srcFolder != null && srcFolder.exists()) {
			if (srcFolder.getProjectRelativePath().isPrefixOf(file.getProjectRelativePath())) {
				moduleName = file.getProjectRelativePath().removeFirstSegments(1).removeFileExtension().toPortableString();
				moduleName = moduleName.replaceAll(Pattern.quote(File.separator), "::").replaceAll("syntax","\\\\syntax");
				return moduleName;
			}
		}
		else {
			moduleName = file.getProjectRelativePath().removeFileExtension().toPortableString();
			moduleName = moduleName.replaceAll(Pattern.quote(File.separator), "::");
			return moduleName;
		}
		
		throw new ImplementationError("could not compute modulename for " + file);
	}
}
