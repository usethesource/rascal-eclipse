package org.rascalmpl.eclipse.editor;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.text.IRegion;

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
