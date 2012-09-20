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
package org.rascalmpl.eclipse.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.language.ILanguageRegistrar;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.NullRascalMonitor;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages;
import org.rascalmpl.parser.gtd.exception.ParseError;

public class InitializeRascalPlugins implements ILanguageRegistrar {
	public void registerLanguages() {
			registerTermLanguagePlugins();
	}
	
	public static void registerTermLanguagePlugins() {
		for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			registerTermLanguagePlugin(project);
		}
	}

	public static void registerTermLanguagePlugin(final IProject project) {
		try {
			if (project.isOpen() && project.hasNature(IRascalResources.ID_RASCAL_NATURE)) {
				IResource pluginRsc = project.findMember("src/Plugin.rsc");
				if (pluginRsc != null) {
					runPluginMain(project);
				}
			}
		}
		catch (CoreException e) {
			Activator.getInstance().logException("could not register any term language plugins", e);
		}
	}

	private static void runPluginMain(final IProject project) {
		Evaluator eval = null;
		try {
			eval = initializeEvaluator(project);

			synchronized(eval){
				eval.doImport(null, "Plugin");
				eval.call(new NullRascalMonitor(), "main");
			}
		}
		catch (RuntimeException e) {
			if (eval != null && (e instanceof ParseError || e instanceof StaticError || e instanceof Throw)) {
				eval.getStdErr().println("Could not run Plugin.rsc main of " + project.getName());
				eval.getStdErr().println(ReadEvalPrintDialogMessages.parseOrStaticOrThrowMessage(e));
			}
			else {
				Activator.getInstance().logException("could not run Plugin.rsc main of " + project.getName(), e);
			}
		}
		catch (Throwable e) {
			Activator.getInstance().logException("could not run Plugin.rsc main of " + project.getName(), e);
		}
	}

	private static Evaluator initializeEvaluator(final IProject project) {
		return ProjectEvaluatorFactory.getInstance().createProjectEvaluator(project);
	}
}
