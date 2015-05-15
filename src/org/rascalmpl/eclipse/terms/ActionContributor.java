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
package org.rascalmpl.eclipse.terms;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.pdb.facts.IBool;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.services.ILanguageActionsContributor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Point;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToErrorLog;
import org.rascalmpl.eclipse.perspective.actions.highlight.ShowAsHTML;
import org.rascalmpl.eclipse.perspective.actions.highlight.ShowAsLatex;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.types.FunctionType;
import org.rascalmpl.interpreter.types.OverloadedFunctionType;
import org.rascalmpl.interpreter.types.RascalTypeFactory;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.ProductionAdapter;
import org.rascalmpl.values.uptr.RascalValueFactory.Tree;
import org.rascalmpl.values.uptr.TreeAdapter;

public class ActionContributor implements ILanguageActionsContributor {
	private static final class Runner extends Action {
		private final UniversalEditor editor;
		private final RascalAction job;
		private final boolean sync;

		public Runner(boolean synchronous, String label, UniversalEditor editor, RascalAction job) {
			super(label);
			
			this.editor = editor;
			this.job = job;
			this.sync = synchronous;
		}
		
		public Runner(boolean checked, boolean synchronous, String label, UniversalEditor editor, RascalAction job) {
			super(label, Action.AS_CHECK_BOX);
			setChecked(checked);
			
			this.editor = editor;
			this.job = job;
			this.sync = synchronous;
		}
		

		public void run() {
			Point selection = editor.getSelection();
			job.init(editor);
			job.schedule();
			if (sync) {
				try {
					job.join();
					if (job.result != null) {
						replaceText(selection, job.result);
					}
				} catch (InterruptedException e) {
					Activator.getInstance().logException("action interrupted", e);
				}
			}	
		}
		
		private void replaceText(Point selection, IString newTree) {
			try {
				String newText = newTree.getValue();
				IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
				doc.replace(0, doc.getLength(), newText);
				if (selection.x < doc.getLength()) {
					editor.selectAndReveal(selection.x, 0);
				}
			} catch (BadLocationException e) {
				Activator.getInstance().logException("could not replace text", e);
			}
		}
	}
	
	private static final class RascalAction extends Job {
		private final ICallableValue func;
		private final WarningsToErrorLog warnings;
		private Tree tree;
		private Point selection;
		public IString result = null;
    

		private RascalAction(String text, ICallableValue func) {
			super(text);
			
			this.func = func;
			this.warnings = new WarningsToErrorLog();
		}
		
		public void init(UniversalEditor editor) {
			this.tree = (Tree) editor.getParseController().getCurrentAst();
			this.selection = editor.getSelection();
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			RascalMonitor rascalMonitor = new RascalMonitor(monitor, warnings);
			
			if (tree != null) {
				Type[] actualTypes = new Type[] { RTF.nonTerminalType(ProductionAdapter.getType(TreeAdapter.getProduction(tree))), TF.sourceLocationType() };
				ISourceLocation loc = TreeAdapter.getLocation(tree);
				IValue[] actuals = new IValue[] { tree, VF.sourceLocation(VF.sourceLocation(loc.getURI()), selection.x, selection.y)};
				try {
					rascalMonitor.startJob("Executing " + getName(), 10000);
					IValue result;
					synchronized(func.getEval()){
						result = func.call(rascalMonitor, actualTypes, actuals, null).getValue();
					}
					
					if ( (func.getType() instanceof OverloadedFunctionType) &&  (((OverloadedFunctionType) func.getType()).getReturnType() != TF.voidType()) ) {
						this.result = (IString) result;
					}
					if ( (func.getType() instanceof FunctionType) && (((FunctionType) func.getType()).getReturnType() != TF.voidType())) {
						this.result = (IString) result;
					}
				}
				catch (Throwable e) {
					Activator.getInstance().logException("error while executing action:" + e.getMessage(), e);
				}
				finally {
					rascalMonitor.endJob(true);
				}
			}
			
			return Status.OK_STATUS;
		}

		
	}

	private final static TypeFactory TF = TypeFactory.getInstance();
	private final static RascalTypeFactory RTF = RascalTypeFactory.getInstance();
	private final static IValueFactory VF = ValueFactoryFactory.getValueFactory();

	public void contributeToEditorMenu(UniversalEditor editor,
			IMenuManager menuManager) {
		ISet contribs = getContribs(editor);
		
		menuManager.add(new ShowAsHTML(editor));
		menuManager.add(new ShowAsLatex(editor));
		
		for (IValue contrib : contribs) {
			IConstructor node = (IConstructor) contrib;
			if (node.getName().equals("popup")) {
				contribute(menuManager, editor, (IConstructor) node.get("menu"));
			}
		}
	}

	private void contribute(IMenuManager menuManager, final UniversalEditor editor, IConstructor menu) {
		String label = ((IString) menu.get("label")).getValue();
		
		if (menu.getName().equals("action") || 
				menu.getName().equals("toggle") ||
				menu.getName().equals("edit")) {
			contributeAction(menuManager, editor, menu, label);
		}
		else if (menu.getName().equals("group")) {
			menuManager.add(new Separator(label));
			for (IValue member : (IList) menu.get("members")) {
				contribute(menuManager, editor, (IConstructor) member);
			}
		}
		else if (menu.getName().equals("menu")) {
			MenuManager sub = new MenuManager(label);
			menuManager.add(sub);
			for (IValue member : (IList) menu.get("members")) {
				contribute(sub, editor, (IConstructor) member);
			}
		}
	}
	
	private boolean getState(ICallableValue func) {
		Type[] actualTypes = new Type[] { };
		IValue[] actuals = new IValue[] { };
		synchronized(func.getEval()){
			func.getEval().__setInterrupt(false);
			return ((IBool) func.call(actualTypes, actuals, null).getValue()).getValue();
		}
		
	}

	
	private void contributeAction(IMenuManager menuManager,
			final UniversalEditor editor, IConstructor menu, String label) {
		if (menu.has("state")) { // toggle, order of evaluation is important as state also has action
			final ICallableValue func = (ICallableValue) menu.get("action");
			menuManager.add(new Runner(getState((ICallableValue) menu.get("state")), true, label, editor, new RascalAction(label, func)));
		}
		else if (menu.has("action")) {
			final ICallableValue func = (ICallableValue) menu.get("action");
			menuManager.add(new Runner(false, label, editor, new RascalAction(label, func)));
		}
		else if (menu.has("edit")) {
			final ICallableValue func = (ICallableValue) menu.get("edit");
			menuManager.add(new Runner(true, label, editor, new RascalAction(label, func)));
		}
	}

	private ISet getContribs(UniversalEditor editor) {
		ISet result = TermLanguageRegistry.getInstance().getContributions(editor.getLanguage());
		if (result == null) {
			result = ValueFactoryFactory.getValueFactory().set();
		}
		return result;
	}

	public void contributeToMenuBar(UniversalEditor editor, IMenuManager menuManager) {
		ISet contribs = getContribs(editor);
		
		for (IValue contrib : contribs) {
			IConstructor node = (IConstructor) contrib;
			if (node.getName().equals("menu")) {
				contribute(menuManager, editor, (IConstructor) node.get("menu"));
			}
		}
	}

	public void contributeToStatusLine(UniversalEditor editor,
			IStatusLineManager statusLineManager) {

	}

	public void contributeToToolBar(UniversalEditor editor,
			IToolBarManager toolbarManager) {

	}

}
