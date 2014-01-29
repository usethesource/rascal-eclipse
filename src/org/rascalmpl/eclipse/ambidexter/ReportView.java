/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Various members of the Software Analysis and Transformation Group - CWI
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package org.rascalmpl.eclipse.ambidexter;

import static org.rascalmpl.eclipse.IRascalResources.ID_AMBIDEXTER_REPORT_VIEW_PART;

import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import nl.cwi.sen1.AmbiDexter.AmbiDexterConfig;
import nl.cwi.sen1.AmbiDexter.IAmbiDexterMonitor;
import nl.cwi.sen1.AmbiDexter.Main;
import nl.cwi.sen1.AmbiDexter.grammar.Character;
import nl.cwi.sen1.AmbiDexter.grammar.Grammar;
import nl.cwi.sen1.AmbiDexter.grammar.NonTerminal;
import nl.cwi.sen1.AmbiDexter.grammar.SymbolString;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.io.StandardTextReader;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.SymbolAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;
import org.rascalmpl.values.uptr.visitors.IdentityTreeVisitor;

public class ReportView extends ViewPart implements IAmbiDexterMonitor {
	public static final String ID = ID_AMBIDEXTER_REPORT_VIEW_PART;
	private static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	private final PrintStream out = RuntimePlugin.getInstance().getConsoleStream();
	private TableColumn nonterminals;
	private TableColumn sentences;
	private Table table;
	private final StandardTextReader reader = new StandardTextReader();
	private volatile boolean canceling;
	private AmbiDexterJob job;
	
	public ReportView() {
		super();
		job = new AmbiDexterJob("AmbiDexter");
	}
	
	public void run(final Grammar grammar, final AmbiDexterConfig cfg) {
		table.removeAll();
		
		println("Running AmbiDexter...");
		grammar.printSize(this);

		canceling = false;		
		job.init(grammar, cfg);
		job.schedule();
	}

	@Override
	public void createPartControl(Composite parent) {
		table = new Table(parent, SWT.BORDER);
	    nonterminals = new TableColumn(table, SWT.CENTER);
	    sentences = new TableColumn(table, SWT.CENTER);
	    nonterminals.setText("Symbol");
	    sentences.setText("Sentence");
	    nonterminals.setWidth(70);
	    sentences.setWidth(70);
	    table.setHeaderVisible(true);

	    IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
	    installAction(toolbar, new EditSentenceAction());
	    installAction(toolbar, new EditTreeAction());
	    installAction(toolbar, new BrowseTreeAction());
	    installAction(toolbar, new DiagnoseAction());
	}

	private void installAction(IToolBarManager toolbar, AbstractAmbidexterAction edit) {
		table.addSelectionListener(edit);
	    toolbar.add(edit);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	@Override
	public void println() {
		out.println();
	}

	@Override
	public void println(Object o) {
		out.println(o);
	}

	@Override
	public void errPrintln() {
		out.println("error:");
	}

	@Override
	public void errPrintln(Object o) {
		out.println("error:" + o);
	}

	@Override
	public void ambiguousString(final AmbiDexterConfig cfg, final SymbolString s, final NonTerminal n, String messagePrefix) {
		try {
			final IConstructor sym = (IConstructor) reader.read(VF, Factory.uptr, Factory.Symbol, new StringReader(n.prettyPrint()));
			final String ascii = toascci(s);
			final String module = getModuleName(cfg.filename);
			final String project = getProjectName(cfg.filename);
			
			addItem(sym, ascii, module, project, null);
		} catch (FactTypeUseException e) {
			Activator.getInstance().logException("failed to register ambiguity", e);
		} catch (IOException e) {
			Activator.getInstance().logException("failed to register ambiguity", e);
		}
	}

	private void addItem(final IConstructor sym, final String ascii,
			final String module, final String project, final IConstructor tree) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(new String[] { SymbolAdapter.toString(sym, false), ascii});
				item.setData("nonterminal", sym);
				item.setData("sentence", ascii);
				item.setData("module", module);
				item.setData("project", project);
				item.setData("tree", tree);
			}
		});
	}

	private String getProjectName(String filename) {
		int i = filename.indexOf('/');
		if (i != -1) {
			return filename.substring(0, i);
		}
		return null;
	}

	private String getModuleName(String filename) {
		int i = filename.indexOf('/');
		if (i != -1) {
			return filename.substring(i+1);
		}
		return null;
	}

	private String toascci(SymbolString s) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < s.size(); i++) {
			 Character ch = (Character) s.get(i);
			 b.append(ch.toAscii());
		}
		return b.toString();
	}

	public void list(final String project, final String module, IConstructor parseTree, final IProgressMonitor monitor) {
		table.removeAll();
		
		try {
			parseTree.accept(new IdentityTreeVisitor<Exception>() {
				int ambTreesSeen = 0;
				@Override
				public IConstructor visitTreeAppl(IConstructor arg)
						throws Exception {
					if (monitor.isCanceled()) {
						throw new Exception("interrupted");
					}
					
					monitor.worked(1);
					
					for (IValue child : TreeAdapter.getArgs(arg)) {
						child.accept(this);
					}
					return arg;
				}
				
				@Override
				public IConstructor visitTreeAmb(IConstructor arg)
						throws Exception {
					ambTreesSeen++;
					if (ambTreesSeen > 100) {
						throw new RuntimeException("Lets stop collecting, the user get's the point, it is VERY ambigious");
					}
					IConstructor sym = null;
					String sentence = TreeAdapter.yield(arg);
					
					for (IValue child : TreeAdapter.getAlternatives(arg)) {
						sym = TreeAdapter.getType((IConstructor) child);
						child.accept(this);
					}
					
					addItem(sym, sentence, module, project, arg);
					return arg;
				}
			});
		} catch (Exception e) {
			// do nothing
		}
	}
	
	@Override
	public void setTaskName(String name, int work) {
		IProgressMonitor m = job.monitor;
		if (m != null) {
			m.beginTask(name, work);
		}
	}

	@Override
	public void worked(int work) {
		IProgressMonitor m = job.monitor;
		if (m != null) {
			m.worked(work);
		}
	}

	@Override
	public boolean canceling() {
		return canceling;
	}

	private class AmbiDexterJob extends Job {
	
		private Main main;
		volatile IProgressMonitor monitor;
	
		public AmbiDexterJob(String name) {
			super(name);
		}
	
		public void init(Grammar grammar, AmbiDexterConfig config) {
			main = new Main(ReportView.this);
			main.setGrammar(grammar);
			main.setConfig(config);
		}
	
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			this.monitor = monitor;
			
			//main.printGrammar();
			try {
			  main.checkGrammar();
			} catch (OutOfMemoryError e) {
			  Activator.log("out of memory while checking grammar for ambiguity", e);
			  main = null; // help the GC in this case
			  System.gc();
			}
			
			monitor.done();
			this.monitor = null;
			return Status.OK_STATUS;
		}
		
		@Override
		protected void canceling() {
			ReportView.this.canceling = true;
		}		
	}
}
