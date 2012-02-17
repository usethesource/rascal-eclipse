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
import java.util.Map.Entry;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.rascalmpl.eclipse.terms.TermLanguageRegistry;
import org.rascalmpl.library.vis.swt.SWTFontsAndColors;

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
	private Boolean firstUse = true;

	private final Map<String,TextAttribute> map = new HashMap<String,TextAttribute>();

	public TokenColorer() {
		super();
		map.put(NORMAL, new TextAttribute(null, null, SWT.NONE));
		map.put(META_KEYWORD, new TextAttribute(new Color(Display.getDefault(), 123, 0, 82), null, SWT.BOLD));
		map.put(META_VARIABLE, new TextAttribute(new Color(Display.getDefault(), 0x29,0x5F,0x94), null, SWT.ITALIC));
		map.put(META_AMBIGUITY,  new TextAttribute(new Color(Display.getDefault(), 186, 29, 29), null, SWT.BOLD));
		map.put(TODO,new TextAttribute(new Color(Display.getDefault(), 123, 157, 198), null, SWT.BOLD));
		map.put(COMMENT,new TextAttribute(new Color(Display.getDefault(), 82, 141, 115), null, SWT.ITALIC));
		map.put(CONSTANT,new TextAttribute(new Color(Display.getDefault(), 139, 0, 139), null, SWT.NONE));
		map.put(VARIABLE,new TextAttribute(new Color(Display.getDefault(), 0x55,0xaa,0x55), null, SWT.NONE));
		map.put(IDENTIFIER,new TextAttribute(new Color(Display.getDefault(), 0x2C,0x57,0x7C), null, SWT.NONE));
		map.put(QUOTE,new TextAttribute(new Color(Display.getDefault(), 255, 69, 0), new Color(Display.getDefault(), 32,178,170), SWT.NONE));
		map.put(TYPE,new TextAttribute(new Color(Display.getDefault(), 0xAB,0x25,0x25), null, SWT.NONE));
	} 

	public IRegion calculateDamageExtent(IRegion seed, IParseController ctlr) {
		return seed;
	}

	public TextAttribute getColoring(IParseController controller, Object token) {
		if (firstUse) {
			firstUse = false;
			ISet contribs = TermLanguageRegistry.getInstance().getContributions(controller.getLanguage());
			if (contribs != null) {
				// check if there might be a category contribution?
				for (IValue contrib : contribs) {
					IConstructor node = (IConstructor) contrib;
					if (node.getName().equals("categories")) {
						extendDefaultCategories(node);
					}
				}
			}
		}
		return map.get(((Token) token).getCategory());
	}

	private void extendDefaultCategories(IConstructor categories) {
		IMap styleMap = (IMap)categories.get("styleMap");
		for (IValue category: styleMap) {
			String categoryName = ((IString)category).getValue();
			TextAttribute textStyle = translate((IConstructor)styleMap.get(category));
			map.put(categoryName, textStyle);
		}
	}

	private TextAttribute translate(IConstructor fontProperties) {
		int style = SWT.NONE;
		for (IValue fs : (ISet)fontProperties.get("style")) {
			String fsName = ((IConstructor)fs).getName();
			if (fsName.equals("bold")) {
				style |= SWT.BOLD;
			}
			else if (fsName.equals("italic")) {
				style |= SWT.ITALIC;
			}
		}
		Font font = null;
		if (fontProperties.has("name")) {
			String fontName = ((IString)fontProperties.get("name")).getValue();
			Integer fontLength = ((IInteger)fontProperties.get("size")).intValue();
			font = new Font(Display.getCurrent(), fontName,  fontLength, style);
		}
		Color foreground = SWTFontsAndColors.getRgbColor(Display.getCurrent(), ((IInteger)fontProperties.get("foregroundColor")).intValue());
		Color background = null;
		if (fontProperties.has("backgroundColor")) {
			background = SWTFontsAndColors.getRgbColor(Display.getCurrent(), ((IInteger)fontProperties.get("backgroundColor")).intValue());
		}
		return new TextAttribute(foreground, background, style, font);
	}
}
