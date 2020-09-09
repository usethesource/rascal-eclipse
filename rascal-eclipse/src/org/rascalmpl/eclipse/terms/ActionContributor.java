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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.function.Function;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.eclipse.ui.progress.UIJob;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.highlight.ShowAsHTML;
import org.rascalmpl.eclipse.editor.highlight.ShowAsLatex;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToErrorLog;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.repl.REPLContentServer;
import org.rascalmpl.repl.REPLContentServerManager;
import org.rascalmpl.types.FunctionType;
import org.rascalmpl.types.RascalTypeFactory;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.parsetrees.ITree;
import org.rascalmpl.values.parsetrees.ProductionAdapter;
import org.rascalmpl.values.parsetrees.TreeAdapter;

import io.usethesource.impulse.editor.UniversalEditor;
import io.usethesource.impulse.services.ILanguageActionsContributor;
import io.usethesource.vallang.IBool;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.type.Type;
import io.usethesource.vallang.type.TypeFactory;

@SuppressWarnings("restriction")
public class ActionContributor implements ILanguageActionsContributor {
    protected final REPLContentServerManager contentManager = new REPLContentServerManager();
    
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
					if (job.result != null && job.result instanceof IString) {
						replaceText(selection, (IString) job.result);
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
	
	private static class RascalAction extends Job {
		private final ICallableValue func;
		private final WarningsToErrorLog warnings;
		private ITree tree;
		private Point selection;
		public IValue result = null;
    

		private RascalAction(String text, ICallableValue func) {
			super(text);
			
			this.func = func;
			this.warnings = new WarningsToErrorLog();
		}
		
		public void init(UniversalEditor editor) {
			this.tree = (ITree) editor.getParseController().getCurrentAst();
			this.selection = editor.getSelection();
		}

		@Override
		public IStatus run(IProgressMonitor monitor) {
			RascalMonitor rascalMonitor = new RascalMonitor(monitor, warnings);
			
			if (tree != null) {
				Type[] actualTypes = new Type[] { RTF.nonTerminalType(ProductionAdapter.getType(TreeAdapter.getProduction(tree))), TF.sourceLocationType() };
				ISourceLocation loc = TreeAdapter.getLocation(tree);
				IValue[] actuals = new IValue[] { tree, VF.sourceLocation(loc, selection.x, selection.y)};
				try {
					rascalMonitor.startJob("Executing " + getName(), 10000);
					IValue result;
					synchronized(func.getEval()){
						result = func.call(rascalMonitor, actualTypes, actuals, null).getValue();
					}
					
					if ( (func.getType() instanceof FunctionType) && (((FunctionType) func.getType()).getReturnType() != TF.voidType())) {
						this.result = result;
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
	
	private static final class RascalContentServerAction extends RascalAction {
        private final REPLContentServerManager servers;

        private RascalContentServerAction(REPLContentServerManager servers, String text, ICallableValue func) {
            super(text, func);
            this.servers = servers;
        }
        
        @Override
        public IStatus run(IProgressMonitor monitor) {
            super.run(monitor);
            try {
                if (result != null && result instanceof IConstructor) {
                    IConstructor provider = (IConstructor) result;
                    Function<IValue, IValue> target;
                    String id;
                    
                    if (provider.has("id")) {
                        id = ((IString) provider.get("id")).getValue();
                        target = liftProviderFunction(provider.get("callback"));
                    }
                    else {
                        id = getName();
                        target = (r) -> provider.get("response");
                    }

                    REPLContentServer server = servers.addServer(id, target);
                    browse(id, "http://localhost:" + server.getListeningPort());
                    
                    result = null; // to avoid substitution side-effect
                }
            } catch (IOException e) {
                Activator.log("could not start interactive visual from action " + getName(), e);
            }

            return Status.OK_STATUS;
        }
        
        private void browse(String id, String host) {
            new UIJob("Content") {
                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    try {
                        URL url = URIUtil.assumeCorrect(host).toURL();
                        IWebBrowser browser = WorkbenchBrowserSupport.getInstance().createBrowser(IWorkbenchBrowserSupport.AS_EDITOR, id, id, "This browser shows the latest web content produced by a Rascal action");
                        browser.openURL(url);
                    } catch (PartInitException | MalformedURLException e) {
                        Activator.log("could not view HTML content", e);
                    }
                    
                    return Status.OK_STATUS;
                }
            }.schedule();
        }
        
        private Function<IValue, IValue> liftProviderFunction(IValue callback) {
            ICallableValue func = (ICallableValue) callback;
            
            return (t) -> {
              synchronized(func.getEval()) {
                  return func.call(
                      new Type[] { REPLContentServer.requestType },
                      new IValue[] { t },
                      Collections.emptyMap()).getValue();
              }
            };
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
				menu.getName().equals("edit") ||
				menu.getName().equals("interaction")) {
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
		else if (menu.has("server")) {
		    final ICallableValue func = (ICallableValue) menu.get("server");
		    menuManager.add(new Runner(false, label, editor, new RascalContentServerAction(contentManager, label, func)));
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
