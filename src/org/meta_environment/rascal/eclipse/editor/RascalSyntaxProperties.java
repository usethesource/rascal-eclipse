package org.meta_environment.rascal.eclipse.editor;

import org.eclipse.imp.services.ILanguageSyntaxProperties;

public class RascalSyntaxProperties implements ILanguageSyntaxProperties {

	@Override
	public String getBlockCommentContinuation() {
		return " * ";
	}

	@Override
	public String getBlockCommentEnd() {
		return "*/";
	}

	@Override
	public String getBlockCommentStart() {
		return "/*";
	}

	@Override
	public String[][] getFences() {
		return new String[][] {
				new String[] { "(", ")" },
				new String[] { "{", "}" },
				new String[] { "<", ">" },
				new String[] { "[", "]" }
		};
	}

	@Override
	public int[] getIdentifierComponents(String ident) {
		return new int[0];
	}

	@Override
	public String getIdentifierConstituentChars() {
		return null;
	}

	@Override
	public String getSingleLineCommentPrefix() {
		return "//";
	}
}
