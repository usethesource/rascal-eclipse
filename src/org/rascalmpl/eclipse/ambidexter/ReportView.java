package org.rascalmpl.eclipse.ambidexter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;

import nl.cwi.sen1.AmbiDexter.AmbiDexterConfig;
import nl.cwi.sen1.AmbiDexter.IAmbiDexterMonitor;
import nl.cwi.sen1.AmbiDexter.Main;
import nl.cwi.sen1.AmbiDexter.grammar.Character;
import nl.cwi.sen1.AmbiDexter.grammar.Grammar;
import nl.cwi.sen1.AmbiDexter.grammar.NonTerminal;
import nl.cwi.sen1.AmbiDexter.grammar.SymbolString;

import org.eclipse.imp.pdb.facts.IConstructor;
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

public class ReportView extends ViewPart implements IAmbiDexterMonitor {
	private static final IValueFactory VF = ValueFactoryFactory.getValueFactory();
	private final PrintStream out = RuntimePlugin.getInstance().getConsoleStream();
	private TableColumn nonterminals;
	private TableColumn sentences;
	private Table table;
	private final StandardTextReader reader = new StandardTextReader();
	
	public ReportView() { super(); }
	
	public void run(final Grammar grammar, final AmbiDexterConfig cfg) {
		table.removeAll();
		
		Runnable run = new Runnable() {
			public void run() {
				Main m = new Main(ReportView.this);
				m.setGrammar(grammar);
				m.setConfig(cfg);
				m.printGrammar(grammar);
				m.checkGrammar(grammar);
			};
		};

		Thread thread = new Thread(run);
		thread.setName("Ambidexter Fred");
		thread.start();
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
	    EditSentenceAction edit = new EditSentenceAction();
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
			final IConstructor sym = (IConstructor) reader.read(VF, Factory.uptr, Factory.Symbol, new ByteArrayInputStream(n.prettyPrint().getBytes()));
			final String ascii = toascci(s);
			final String module = getModuleName(cfg.filename);
			final String project = getProjectName(cfg.filename);
			
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(new String[] { SymbolAdapter.toString(sym), ascii});
					item.setData("nonterminal", sym);
					item.setData("sentence", ascii);
					item.setData("module", module);
					item.setData("project", project);
				}
			});
		} catch (FactTypeUseException e) {
			Activator.getInstance().logException("failed to register ambiguity", e);
		} catch (IOException e) {
			Activator.getInstance().logException("failed to register ambiguity", e);
		}
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
}
