/*******************************************************************************
 * Copyright (c) 2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Anya Helene Bagge - anya@ii.uib.no - UiB
 *******************************************************************************/
package org.rascalmpl.eclipse.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RascalKeywords {
	public static final Set<String> keywords = new HashSet<String>(Arrays.asList("alias", "all", "anno", "any",
			"append", "assert", "assoc", "bag", "bool", "bracket", "break",
			"case", "catch", "continue", "data", "datetime", "default",
			"dynamic", "else", "extend", "fail", "false", "filter", "finally",
			"for", "if", "import", "in", "insert", "int", "it", "join",
			"keyword", "layout", "lexical", "list", "loc", "lrel", "map",
			"mod", "module", "node", "non-assoc", "notin", "num", "o", "one",
			"private", "public", "rat", "real", "rel", "return", "set",
			"solve", "start", "str", "switch", "syntax", "tag", "test",
			"throw", "throws", "true", "try", "tuple", "type",
			"value", "visit", "void", "while"));

	/**
	 * Escape (parts of) a name by prefixing Rascal keywords by a backslash.
	 *  
	 * @param name A (possibly qualified) name which might use Rascal keywords
	 * @return An escaped name
	 */
	public static String escapeName(String name) {
		String[] split = name.split("::");
		StringBuilder b = new StringBuilder(name.length() + split.length);
		for(int i = 0; i < split.length; i++) {
			if(i > 0) {
				b.append("::");
			}
			if(keywords.contains(split[i])) {
				b.append("\\");
			}
			b.append(split[i]);
		}
		name = b.toString();

		return name;
	}
}
