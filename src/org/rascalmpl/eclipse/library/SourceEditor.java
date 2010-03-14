package org.rascalmpl.eclipse.library;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.terms.TermLanguageRegistry;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.result.OverloadedFunctionResult;

public class SourceEditor {
	public SourceEditor(IValueFactory factory) {
	}
	
	public void registerLanguage(IString name, IString extension, IConstructor start, IEvaluatorContext ctx) {
		TermLanguageRegistry.getInstance().registerLanguage(name.getValue(), extension.getValue(), start, ctx);
	}
	
	public void registerAnnotator(IString name, IValue function) {
		if (function instanceof OverloadedFunctionResult) {
			TermLanguageRegistry.getInstance().registerAnnotator(name.getValue(), (OverloadedFunctionResult) function);
			return;
		}
		Activator.getInstance().logException("could not register analysis for " + name, new RuntimeException());
	}
}
