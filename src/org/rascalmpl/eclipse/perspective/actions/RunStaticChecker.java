/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
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
package org.rascalmpl.eclipse.perspective.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.builder.MarkerCreator;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.IProgressService;
import org.rascalmpl.checker.StaticChecker;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.MessagesToMarkers;
import org.rascalmpl.eclipse.editor.ParseController;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.values.uptr.ITree;
import org.rascalmpl.values.uptr.TreeAdapter;

public class RunStaticChecker extends AbstractEditorAction {
	private final MessagesToMarkers marker = new MessagesToMarkers();
	private final StaticCheckerHelper helper = new StaticCheckerHelper();
	
	public RunStaticChecker(UniversalEditor editor) {
		super(editor, "Run static checker");
	}

	@Override
	public void run() {
		final IMessageHandler handler = new MarkerCreator(file);
		
		if (editor != null) {
			WorkspaceModifyOperation wmo = new WorkspaceModifyOperation(ResourcesPlugin.getWorkspace().getRoot()) {
				public void execute(IProgressMonitor monitor) {
					ITree toCheck = (ITree)editor.getParseController().getCurrentAst();
					ITree res = check(toCheck,editor.getParseController(), handler);
					((ParseController) editor.getParseController()).setCurrentAst(res);
				}
			};
			IProgressService ips = PlatformUI.getWorkbench().getProgressService();
			try {
				ips.run(true, true, wmo);
			} catch (InvocationTargetException e) {
				Activator.getInstance().logException("??", e);
			} catch (InterruptedException e) {
				Activator.getInstance().logException("??", e);
			}
		}
	}

	public ITree check(ITree parseTree, final IParseController parseController, final IMessageHandler handler) {
		if (parseTree == null) return null;
						
		try {
			StaticChecker checker = helper.createCheckerIfNeeded(parseController.getProject());
			
			if (checker != null) {
				ITree newTree = checker.checkModule(null, (ITree) TreeAdapter.getArgs(parseTree).get(1));
				if (newTree != null) {
					ITree treeTop = parseTree;
					IList treeArgs = TreeAdapter.getArgs(treeTop).put(1, newTree);
					ITree newTreeTop = TreeAdapter.setLocation(TreeAdapter.setArgs(treeTop, treeArgs), TreeAdapter.getLocation(treeTop)).asWithKeywordParameters().setParameter("docStrings", newTree.asWithKeywordParameters().getParameter("docStrings")).asWithKeywordParameters().setParameter("docLinks", newTree.asWithKeywordParameters().getParameter("docLinks"));
					parseTree = newTreeTop;
					handler.clearMessages();
					marker.process(parseTree, handler); 
					handler.endMessages();
				}
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
		
		return parseTree;
	}	

}
