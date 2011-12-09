package org.rascalmpl.eclipse.ambidexter;

import nl.cwi.sen1.AmbiDexter.AmbiDexterConfig;
import nl.cwi.sen1.AmbiDexter.grammar.Grammar;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IRelation;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class AmbiDexterRunner {
	public static void run(AmbiDexterConfig cfg, IConstructor grammar, IRelation nestingRestr) {
		try {
			AmbiDexterConfig.verbose = true;
			Grammar g = new GrammarBuilder().build(grammar, nestingRestr, cfg);
			
			ReportView part = (ReportView) PlatformUI.getWorkbench()
		    .getActiveWorkbenchWindow()
		    .getActivePage()
			.showView("rascal-eclipse.ambidexter.report");
			
			part.run(g, cfg);
		} catch (InvalidInputException e) {
			RuntimePlugin.getInstance().logException("could not run ambidexter", e);
		} catch (PartInitException e) {
			RuntimePlugin.getInstance().logException("could not run ambidexter", e);
		}
	}
}
