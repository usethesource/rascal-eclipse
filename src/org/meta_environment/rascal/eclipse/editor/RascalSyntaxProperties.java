package org.meta_environment.rascal.eclipse.editor;

import org.eclipse.imp.services.ILanguageSyntaxProperties;

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
}
