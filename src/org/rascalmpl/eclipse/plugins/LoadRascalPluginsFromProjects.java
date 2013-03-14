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

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.imp.language.ILanguageRegistrar;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.util.RascalEclipseManifest;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.NullRascalMonitor;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.interpreter.utils.ReadEvalPrintDialogMessages;
import org.rascalmpl.parser.gtd.exception.ParseError;

public class LoadRascalPluginsFromProjects implements ILanguageRegistrar {
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
			  RascalEclipseManifest mf = new RascalEclipseManifest();
			  String mainModule = mf.getMainModule(project);
			  String mainFunction = mf.getMainFunction(project);
			  List<String> roots = mf.getSourceRoots(project);
			  
			  for (String root : roots) {
			    IResource pluginRsc = project.findMember(root + "/" + mainModule + IRascalResources.RASCAL_EXT);
			    if (pluginRsc != null) {
			      runPluginMain(project, mainModule, mainFunction);
			      break;
			    }
			  }
			}
		}
		catch (CoreException e) {
			Activator.getInstance().logException("could not register any term language plugins", e);
		}
	}

	private static void runPluginMain(final IProject project, String mainModule, String mainFunction) {
		Evaluator eval = null;
		try {
			eval = initializeEvaluator(project);

			synchronized(eval){
				eval.doImport(null, mainModule);
				eval.call(new NullRascalMonitor(), mainFunction);
			}
		}
		catch (ParseError | StaticError | Throw e) {
		  eval.getStdErr().println("Could not run Plugin.rsc main of " + project.getName());
      eval.getStdErr().println(ReadEvalPrintDialogMessages.parseOrStaticOrThrowMessage(e));
		}
		catch (Throwable e) {
			Activator.getInstance().logException("could not run Plugin.rsc main of " + project.getName(), e);
		}
	}

	private static Evaluator initializeEvaluator(final IProject project) {
		return ProjectEvaluatorFactory.getInstance().createProjectEvaluator(project);
	}
}
