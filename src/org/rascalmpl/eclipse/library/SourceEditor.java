package org.rascalmpl.eclipse.library;

import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.terms.TermLanguageRegistry;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.result.AbstractFunction;

public class SourceEditor {
	public SourceEditor(IValueFactory factory) {
	}
	
	public void registerLanguage(IString name, IString extension, IValue parser, IEvaluatorContext ctx) {
		TermLanguageRegistry.getInstance().registerLanguage(name.getValue(), extension.getValue(), parser, ctx);
	}
	
	public void registerAnnotator(IString name, IValue function) {
		if (function instanceof AbstractFunction) {
			TermLanguageRegistry.getInstance().registerAnnotator(name.getValue(), (AbstractFunction) function);
			return;
		}
		Activator.getInstance().logException("could not register analysis for " + name, new RuntimeException());
	}
}
