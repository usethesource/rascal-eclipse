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
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class TokenColorer implements ITokenColorer {
	public static final String NORMAL = "Normal";
	public static final String TYPE = "Type";
	public static final String IDENTIFIER = "Identifier";
	public static final String VARIABLE = "Variable";
	public static final String CONSTANT = "Constant";
	public static final String COMMENT = "Comment";
	public static final String TODO = "Todo";
	public static final String QUOTE = "Quote";
	public static final String META_AMBIGUITY = "MetaAmbiguity";
	public static final String META_VARIABLE = "MetaVariable";
	public static final String META_KEYWORD = "MetaKeyword";

	private final Map<String,TextAttribute> map = new HashMap<String,TextAttribute>();

	public TokenColorer() {
		super();
		// solarized color template imported
		map.put(NORMAL, new TextAttribute(null, null, SWT.NONE));
		map.put(META_KEYWORD, new TextAttribute(new Color(Display.getDefault(), 0x85,0x99,0x00), null, SWT.BOLD));
		map.put(META_VARIABLE, new TextAttribute(new Color(Display.getDefault(),0x46,0x91,0x86), null, SWT.ITALIC));
		map.put(META_AMBIGUITY,  new TextAttribute(new Color(Display.getDefault(), 186, 29, 29), null, SWT.BOLD));
		map.put(TODO,new TextAttribute(new Color(Display.getDefault(), 0xd3,0x36,0x82), null, SWT.BOLD));
		map.put(COMMENT,new TextAttribute(new Color(Display.getDefault(), 0x93,0xA1,0xA1), null, SWT.ITALIC));
		map.put(CONSTANT,new TextAttribute(new Color(Display.getDefault(), 0x2A,0xA1,0x98), null, SWT.NONE));
		map.put(VARIABLE,new TextAttribute(new Color(Display.getDefault(), 0x26,0x8B,0xD2), null, SWT.NONE));
		map.put(IDENTIFIER,new TextAttribute(new Color(Display.getDefault(), 0x46,0x91,0x86/* 0x26,0x8b,0xd2*/), null, SWT.NONE));
		map.put(QUOTE,new TextAttribute(new Color(Display.getDefault(), 0x2A,0xA1,0x98), null, SWT.NONE));
		map.put(TYPE,new TextAttribute(new Color(Display.getDefault(), 0xA5,0x78,0x00), null, SWT.NONE));
	} 

	public IRegion calculateDamageExtent(IRegion seed, IParseController ctlr) {
		return seed;
	}

	public TextAttribute getColoring(IParseController controller, Object token) {
		return map.get(((Token) token).getCategory());
	}
}
