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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.imp.builder.MarkerCreator;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.model.ISourceProject;
import org.eclipse.imp.parser.IMessageHandler;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.IProgressService;
import org.rascalmpl.checker.StaticChecker;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.MessagesToMarkers;
import org.rascalmpl.eclipse.editor.ParseController;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.staticErrors.StaticError;
import org.rascalmpl.values.uptr.TreeAdapter;

public class RunStaticChecker implements IEditorActionDelegate {
	private final MessagesToMarkers marker = new MessagesToMarkers();
	private final StaticCheckerHelper helper = new StaticCheckerHelper();
	
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
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath path = editor.getParseController().getPath();
		ISourceProject project = editor.getParseController().getProject();
		IFile file;
		
		if (project != null) {
			file = project.getRawProject().getFile(path);
		}
		else {
			file = workspaceRoot.getFile(path);
		}
		
		
		final IMessageHandler handler = new MarkerCreator(file);
		
		if (editor != null) {
			WorkspaceModifyOperation wmo = new WorkspaceModifyOperation(ResourcesPlugin.getWorkspace().getRoot()) {
				public void execute(IProgressMonitor monitor) {
					IConstructor toCheck = (IConstructor)editor.getParseController().getCurrentAst();
					IConstructor res = check(toCheck,editor.getParseController(), handler);
					((ParseController) editor.getParseController()).setCurrentAst(res);
				}
			};
			IProgressService ips = PlatformUI.getWorkbench().getProgressService();
			try {
				ips.run(true, true, wmo);
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public IConstructor check(IConstructor parseTree, final IParseController parseController, final IMessageHandler handler) {
		if (parseTree == null) return null;
						
		try {
			StaticChecker checker = helper.createCheckerIfNeeded(parseController.getProject());
			
			if (checker != null) {
				IConstructor newTree = checker.checkModule(null, (IConstructor) TreeAdapter.getArgs(parseTree).get(1));
				if (newTree != null) {
					IConstructor treeTop = parseTree;
					IList treeArgs = TreeAdapter.getArgs(treeTop).put(1, newTree);
					IConstructor newTreeTop = treeTop.set("args", treeArgs).setAnnotation("loc", treeTop.getAnnotation("loc")).setAnnotation("docStrings", newTree.getAnnotation("docStrings")).setAnnotation("docLinks", newTree.getAnnotation("docLinks"));
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
