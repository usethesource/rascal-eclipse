package org.rascalmpl.eclipse.ambidexter;

import java.io.PrintStream;

import nl.cwi.sen1.AmbiDexter.AmbiDexterConfig;
import nl.cwi.sen1.AmbiDexter.IAmbiDexterMonitor;
import nl.cwi.sen1.AmbiDexter.Main;
import nl.cwi.sen1.AmbiDexter.grammar.Grammar;
import nl.cwi.sen1.AmbiDexter.grammar.NonTerminal;
import nl.cwi.sen1.AmbiDexter.grammar.SymbolString;

import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.UIPlugin;
import org.eclipse.ui.part.ViewPart;

public class ReportView extends ViewPart implements IAmbiDexterMonitor {
	private final PrintStream out = RuntimePlugin.getInstance().getConsoleStream();
	private TableColumn nonterminals;
	private TableColumn sentences;
	private boolean initialized = false;
	private Table table;
	
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
	    nonterminals.setText("Non-terminal");
	    sentences.setText("Sentence");
	    nonterminals.setWidth(70);
	    sentences.setWidth(70);
	    table.setHeaderVisible(true);
	    initialized = true;
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
	public void ambiguousString(final SymbolString s, final NonTerminal n, String messagePrefix) {
		if (!initialized) {
			println(n + ":" + s);
			throw new RuntimeException("reporting ambiguity before the view has been initialized");
		}
		else {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText(new String[] { n.prettyPrint(), s.prettyPrint()});
				}
			});
		}
	}
}
