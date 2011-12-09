package org.rascalmpl.eclipse.ambidexter;

import nl.cwi.sen1.AmbiDexter.AmbiDexterConfig;
import nl.cwi.sen1.AmbiDexter.IAmbiDexterMonitor;
import nl.cwi.sen1.AmbiDexter.Main;
import nl.cwi.sen1.AmbiDexter.grammar.Grammar;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.jdt.core.compiler.InvalidInputException;

public class AmbiDexterRunner {

	public static void run(AmbiDexterConfig cfg, IConstructor grammar, IRelation nestingRestr) {
		try {
			AmbiDexterConfig.verbose = true;
			Grammar g = new GrammarBuilder().build(grammar, nestingRestr, cfg);
			
			IAmbiDexterMonitor monitor = new EclipseMonitor();
			Main m = new Main(monitor);
			m.setGrammar(g);
			m.setConfig(cfg);
			m.checkGrammar(g);
		} catch (InvalidInputException e) {
			e.printStackTrace();
		}
	}
}
