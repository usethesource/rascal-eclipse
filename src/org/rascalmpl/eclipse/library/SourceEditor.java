package org.rascalmpl.eclipse.library;

import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.terms.TermLanguageRegistry;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.result.ICallableValue;

public class SourceEditor {
	public SourceEditor(IValueFactory factory) {
	}
	
	public void registerLanguage(IString name, IString extension, IValue parser, IEvaluatorContext ctx) {
		TermLanguageRegistry.getInstance().registerLanguage(name.getValue(), extension.getValue(), (ICallableValue) parser, ctx);
	}
	
	public void registerAnnotator(IString name, IValue function) {
		if (function instanceof ICallableValue) {
			TermLanguageRegistry.getInstance().registerAnnotator(name.getValue(), (ICallableValue) function);
			return;
		}
		Activator.getInstance().logException("could not register analysis for " + name, new RuntimeException());
	}
	
	public void registerOutliner(IString name, IValue builder) {
		if (builder instanceof ICallableValue) {
			TermLanguageRegistry.getInstance().registerOutliner(name.getValue(), (ICallableValue) builder);
		}
		else {
			Activator.getInstance().logException("could not register tree model builder for " + name, new RuntimeException());
		}
	}
	
	public void registerContributions(IString name, ISet contributions) {
		TermLanguageRegistry.getInstance().registerContributions(name.getValue(),contributions);
	}
	
	public void clearLanguages() {
		TermLanguageRegistry.getInstance().clear();
	}
	
	public void clearLanguage(IString name) {
		TermLanguageRegistry.getInstance().clear(name.getValue());
	}
}
