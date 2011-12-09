package org.rascalmpl.eclipse.ambidexter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.cwi.sen1.AmbiDexter.AmbiDexterConfig;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.jface.wizard.Wizard;

public class AmbiDexterWizard extends Wizard {

	private AmbiDexterWizardPage page1;
	IConstructor grammar;
	AmbiDexterConfig config;

	public AmbiDexterWizard(String moduleName, IConstructor grammar) {
		super();
		setNeedsProgressMonitor(true);
		this.grammar = grammar;
	}

	@Override
	public void addPages() {
		page1 = new AmbiDexterWizardPage();
		addPage(page1);

		List<String> startSymbols = new ArrayList<String>();
		List<String> otherSymbols = new ArrayList<String>();
		new GrammarBuilder().getSymbolNames(grammar, startSymbols, otherSymbols);
		Collections.sort(startSymbols);
		Collections.sort(otherSymbols);
		
		String[] symbols = new String[startSymbols.size() + otherSymbols.size()];
		int i = 0;
		for (String s : startSymbols) {
			symbols[i++] = s;
		}
		for (String s : otherSymbols) {
			symbols[i++] = s;
		}
		page1.startSymbols = symbols;
	}

	@Override
	public boolean performFinish() {
		config = page1.getSettings();
		return true;
	}

	public AmbiDexterConfig getConfig() {
		return config;
	}
}
