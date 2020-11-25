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

import org.rascalmpl.eclipse.terms.TermLanguageRegistry;
import org.rascalmpl.values.functions.IFunction;

import io.usethesource.vallang.ISet;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.IValueFactory;

public class IDE {
	public IDE(IValueFactory factory) {
	}
	
	public void registerLanguage(IString name, IString extension, IFunction parser) {
		TermLanguageRegistry.getInstance().registerLanguage(name.getValue(), extension.getValue(), parser);
	}

	public void registerAnnotator(IString name, IFunction function) {
		TermLanguageRegistry.getInstance().registerAnnotator(name.getValue(), function);
	}
	
	public void registerOutliner(IString name, IFunction builder) {
		TermLanguageRegistry.getInstance().registerOutliner(name.getValue(), builder);
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
}
