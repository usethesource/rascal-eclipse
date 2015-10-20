/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.editor;

import org.eclipse.jface.text.IRegion;

import io.usethesource.impulse.parser.IParseController;
import io.usethesource.impulse.services.ILanguageSyntaxProperties;

public class RascalSyntaxProperties implements ILanguageSyntaxProperties {

	public String getBlockCommentContinuation() {
		return " * ";
	}

	public String getBlockCommentEnd() {
		return "*/";
	}

	public String getBlockCommentStart() {
		return "/*";
	}

	public String[][] getFences() {
		return new String[][] {
				new String[] { "(", ")" },
				new String[] { "{", "}" },
				new String[] { "<", ">" },
				new String[] { "[", "]" }
		};
	}

	public int[] getIdentifierComponents(String ident) {
		return new int[0];
	}

	public String getIdentifierConstituentChars() {
		return null;
	}

	public String getSingleLineCommentPrefix() {
		return "//";
	}

	public IRegion getDoubleClickRegion(int offset, IParseController pc) {
		return null;
	}

	public boolean isIdentifierPart(char ch) {
		return Character.isJavaIdentifierPart(ch);
	}

	public boolean isIdentifierStart(char ch) {
		return Character.isJavaIdentifierStart(ch);
	}

	public boolean isWhitespace(char ch) {
		return ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r';
	}
}
