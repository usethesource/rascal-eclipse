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
	public static final String TYPE = "Type";
	public static final String IDENTIFIER = "Identifier";
	public static final String VARIABLE = "Variable";
	public static final String CONSTANT = "Constant";
	public static final String COMMENT = "Comment";
	public static final String TODO = "Todo";
	public static final String META_AMBIGUITY = "MetaAmbiguity";
	public static final String META_VARIABLE = "MetaVariable";
	public static final String META_KEYWORD = "MetaKeyword";

	private final Map<String,TextAttribute> map = new HashMap<String,TextAttribute>();

	public TokenColorer() {
		super();
	    map.put(META_KEYWORD, new TextAttribute(new Color(Display.getDefault(), 123, 0, 82), null, SWT.BOLD));
	    map.put(META_VARIABLE, new TextAttribute(new Color(Display.getDefault(), 0, 0, 255), null, SWT.ITALIC));
	    //map.put(META_AMBIGUITY,  new TextAttribute(new Color(Display.getDefault(), 186, 29, 29), null, SWT.BOLD));
		map.put(TODO,new TextAttribute(new Color(Display.getDefault(), 123, 157, 198), null, SWT.BOLD));
		map.put(COMMENT,new TextAttribute(new Color(Display.getDefault(), 82, 141, 115), null, SWT.ITALIC));
		map.put(CONSTANT,new TextAttribute(new Color(Display.getDefault(), 139, 0, 139), null, SWT.NONE));
		map.put(VARIABLE,new TextAttribute(new Color(Display.getDefault(), 144, 238, 144), null, SWT.NONE));
		map.put(IDENTIFIER,new TextAttribute(new Color(Display.getDefault(), 255, 69, 0), null, SWT.NONE));
		map.put(TYPE,new TextAttribute(new Color(Display.getDefault(), 255, 127, 36), null, SWT.NONE));
	}

	public IRegion calculateDamageExtent(IRegion seed, IParseController ctlr) {
		return seed;
	}

	public TextAttribute getColoring(IParseController controller, Object token) {
		return map.get(((Token) token).getCategory());
	}
}
