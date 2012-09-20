/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Tijs van der Storm - Tijs.van.der.Storm@cwi.nl
 *   * Davy Landman - Davy.Landman@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.library.util;

import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.rascalmpl.eclipse.console.CustomConsoleRegistry;
import org.rascalmpl.eclipse.terms.TermLanguageRegistry;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.result.ICallableValue;

public class IDE {
	private final IValueFactory VF;
	private static final TypeFactory TF = TypeFactory.getInstance();
	public IDE(IValueFactory factory) {
		VF = factory;
	}
	
	public void registerLanguage(IString name, IString extension, IValue parser, IEvaluatorContext ctx) {
		TermLanguageRegistry.getInstance().registerLanguage(name.getValue(), extension.getValue(), (ICallableValue) parser, ctx);
	}
	
	public void registerAnnotator(IString name, IValue function) {
		TermLanguageRegistry.getInstance().registerAnnotator(name.getValue(), (ICallableValue) function);
	}
	
	public void registerOutliner(IString name, IValue builder) {
		TermLanguageRegistry.getInstance().registerOutliner(name.getValue(), (ICallableValue) builder);
	}
	
	public void registerContributions(IString name, ISet contributions) {
		TermLanguageRegistry.getInstance().registerContributions(name.getValue(),contributions);
	}
	
	public void registerNonRascalContributions(IString name, ISet contributions) {
		TermLanguageRegistry.getInstance().registerNonRascalContributions(name.getValue(),contributions);
	}
	
	public void clearNonRascalContributions() {
		TermLanguageRegistry.getInstance().clearNonRascal();
	}

	public void clearNonRascalContribution(IString name) {
		TermLanguageRegistry.getInstance().clearNonRascal(name.getValue());
	}

	
	public void clearLanguages() {
		TermLanguageRegistry.getInstance().clear();
	}
	
	public void clearLanguage(IString name) {
		TermLanguageRegistry.getInstance().clear(name.getValue());
	}
	
	public void createConsole(IString name, IString startText, IValue newLineCallback) {
		CustomConsoleRegistry.getInstance().registerConsole(name.getValue(), startText.getValue(), TF, VF, (ICallableValue)newLineCallback);
	}
}
