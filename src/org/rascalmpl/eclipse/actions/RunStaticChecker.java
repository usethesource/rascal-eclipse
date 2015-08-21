/*******************************************************************************
 * Copyright (c) 2009-2015 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.builder.MarkerCreator;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.IProgressService;
import org.rascalmpl.checker.StaticChecker;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.MessagesToMarkers;
import org.rascalmpl.eclipse.editor.ParseController;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToMarkers;
import org.rascalmpl.eclipse.uri.ProjectURIResolver;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.values.uptr.ITree;

public class RunStaticChecker extends AbstractEditorAction {
	private final MessagesToMarkers marker = new MessagesToMarkers();
	private final StaticCheckerHelper helper = new StaticCheckerHelper();
	
	public RunStaticChecker(UniversalEditor editor) {
		super(editor, "Run static checker");
	}

	@Override
	public void run() {
		if (editor != null) {
			final IMessageHandler handler = new MarkerCreator(file);
			
			WorkspaceModifyOperation wmo = new WorkspaceModifyOperation(ResourcesPlugin.getWorkspace().getRoot()) {
				public void execute(IProgressMonitor monitor) {
					ITree res = check(monitor, file.getProject(), ProjectURIResolver.constructProjectURI(file.getFullPath()), handler);
					if (res != null) {
						// for highlighting and such
						((ParseController) editor.getParseController()).setCurrentAst(res);
					}
				}
			};
			
			IProgressService ips = PlatformUI.getWorkbench().getProgressService();
			try {
				ips.run(true, true, wmo);
			} catch (InvocationTargetException | InterruptedException e) {
				Activator.getInstance().logException("unexpected error while running static checker", e);
			} 
		}
	}

	public ITree check(IProgressMonitor monitor, IProject project, final ISourceLocation source, final IMessageHandler handler) {
						
		try {
			RascalMonitor mon = new RascalMonitor(monitor, new WarningsToMarkers());
			StaticChecker checker = helper.createCheckerIfNeeded(mon, project);
			
			if (checker != null) {
				
				ITree newTree = checker.checkModule(mon, source);
				if (newTree != null) {
					handler.clearMessages();
					marker.process(newTree, handler);
					handler.endMessages();
				}
				
				return newTree;
			} else {
				Activator.getInstance().logException("static checker could not be created", new RuntimeException());
			}
		}
		catch (StaticError e) {
			Activator.getInstance().logException(e.getLocation() + e.getMessage(), e);
		}
		catch (Throw e) {
			Activator.getInstance().logException(e.getException().toString(), e);
			System.err.println(e.getMessage() + "\n" + e.getTrace());
		}
		catch (Throwable e) {
			Activator.getInstance().logException("static checker failed", e);
		}
		
		return null;
	}	
}
