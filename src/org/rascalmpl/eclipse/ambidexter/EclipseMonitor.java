package org.rascalmpl.eclipse.ambidexter;

import nl.cwi.sen1.AmbiDexter.AmbiDexterConfig;
import nl.cwi.sen1.AmbiDexter.IAmbiDexterMonitor;
import nl.cwi.sen1.AmbiDexter.grammar.NonTerminal;
import nl.cwi.sen1.AmbiDexter.grammar.SymbolString;

import org.eclipse.imp.runtime.RuntimePlugin;

public class EclipseMonitor implements IAmbiDexterMonitor {

	public EclipseMonitor() {
	}
	
	@Override
	public void println() {
		RuntimePlugin.getInstance().getConsoleStream().println();
	}

	@Override
	public void println(Object o) {
		RuntimePlugin.getInstance().getConsoleStream().println(o);
	}

	@Override
	public void errPrintln() {
		RuntimePlugin.getInstance().getConsoleStream().println();
	}

	@Override
	public void errPrintln(Object o) {
		RuntimePlugin.getInstance().getConsoleStream().println(o);
	}

	@Override
	public void ambiguousString(AmbiDexterConfig cfg, SymbolString s, NonTerminal n, String messagePrefix) {
		println(messagePrefix + "Ambiguity found for " + n.prettyPrint() + ": " + s.prettyPrint());
	}
	
	@Override
	public void setTaskName(String name, int work) {
		// ignore
	}

	@Override
	public void worked(int work) {
		// ignore
	}
	
	@Override
	public boolean canceling() {
		return false;
	}

}
