package org.rascalmpl.eclipse.ambidexter;

import org.eclipse.imp.runtime.RuntimePlugin;

import nl.cwi.sen1.AmbiDexter.IAmbiDexterMonitor;
import nl.cwi.sen1.AmbiDexter.grammar.NonTerminal;
import nl.cwi.sen1.AmbiDexter.grammar.SymbolString;

public class EclipseMonitor implements IAmbiDexterMonitor {

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
	public void ambiguousString(SymbolString s, NonTerminal n, String messagePrefix) {
		println(messagePrefix + "Ambiguity found for " + n.prettyPrint() + ": " + s.prettyPrint());
	}

}
