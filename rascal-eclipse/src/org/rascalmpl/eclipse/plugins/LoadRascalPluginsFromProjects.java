/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Anya Helene Bagge - A.H.S.Bagge@cwi.nl (Univ. Bergen)
*******************************************************************************/
package org.rascalmpl.eclipse.plugins;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.preferences.RascalPreferences;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;
import org.rascalmpl.exceptions.Throw;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.NullRascalMonitor;
import org.rascalmpl.interpreter.staticErrors.ModuleImport;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages;
import org.rascalmpl.parser.gtd.exception.ParseError;

import io.usethesource.impulse.language.ILanguageRegistrar;
import io.usethesource.vallang.io.StandardTextWriter;

public class LoadRascalPluginsFromProjects implements ILanguageRegistrar {
	public void registerLanguages() {
	    if (RascalPreferences.loadInterpretedLanguagesFromProjects()) {
	        registerTermLanguagePlugins();
	    }
	}
	
	public static void registerTermLanguagePlugins() {
		for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			registerTermLanguagePlugin(project);
		}
	}

	public static void registerTermLanguagePlugin(final IProject project) {
		try {
			if (project.isOpen() && project.hasNature(IRascalResources.ID_RASCAL_NATURE)) {
			  RascalEclipseManifest mf = new RascalEclipseManifest();
			  
			  if (mf.hasManifest(project)) {
			    String mainModule = mf.getMainModule(project);
			    String mainFunction = mf.getMainFunction(project);

			    if (mainModule != null && mainFunction != null) {
			      runPluginMain(project, mainModule, mainFunction);
			    }
			  }
			}
		}
		catch (CoreException e) {
			Activator.getInstance().logException("could not register any term language plugins", e);
		}
	}
	
	private static void runPluginMain(final IProject project, String mainModule, String mainFunction) {
		Evaluator eval = ProjectEvaluatorFactory.getInstance().getEvaluator(project); 
		
		try {
			synchronized(eval){
				eval.doImport(null, mainModule);
				eval.call(new NullRascalMonitor(), mainFunction);
			}
		}
		catch (ModuleImport e) {
			if (e.getLocation().getScheme().equals("import")) {
				// we can ignore because this is just the main Plugin module not existing
				return;
			}
			eval.getErrorPrinter().println("Could not run Plugin.rsc main of " + project.getName());
			ReadEvalPrintDialogMessages.parseOrStaticOrThrowMessage(eval.getErrorPrinter(), e, new StandardTextWriter(true));
			eval.getErrorPrinter().println();
		}
		catch (ParseError | StaticError | Throw e) {
		  eval.getErrorPrinter().println("Could not run Plugin.rsc main of " + project.getName());
			ReadEvalPrintDialogMessages.parseOrStaticOrThrowMessage(eval.getErrorPrinter(), e, new StandardTextWriter(true));
			eval.getErrorPrinter().println();
		}
		catch (Throwable e) {
			Activator.getInstance().logException("could not run Plugin.rsc main of " + project.getName(), e);
		}
	}
}
