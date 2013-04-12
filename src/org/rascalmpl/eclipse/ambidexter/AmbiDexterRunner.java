package org.rascalmpl.eclipse.ambidexter;

import nl.cwi.sen1.AmbiDexter.AmbiDexterConfig;
import nl.cwi.sen1.AmbiDexter.grammar.Grammar;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class AmbiDexterRunner {
	public static void run(AmbiDexterConfig cfg, IConstructor grammar, ISet nestingRestr) {
		try {
			Grammar g = new GrammarBuilder().build(grammar, nestingRestr, cfg);
			
			ReportView part = (ReportView) PlatformUI.getWorkbench()
		    .getActiveWorkbenchWindow()
		    .getActivePage()
			.showView(ReportView.ID);
			
			part.run(g, cfg);
		} catch (InvalidInputException e) {
			RuntimePlugin.getInstance().logException("could not run ambidexter", e);
		} catch (PartInitException e) {
			RuntimePlugin.getInstance().logException("could not run ambidexter", e);
		}
	}
}
