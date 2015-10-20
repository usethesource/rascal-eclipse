package org.rascalmpl.eclipse.ambidexter;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.rascalmpl.value.IConstructor;
import org.rascalmpl.value.ISet;

import io.usethesource.impulse.runtime.RuntimePlugin;
import nl.cwi.sen1.AmbiDexter.AmbiDexterConfig;
import nl.cwi.sen1.AmbiDexter.grammar.Grammar;

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
