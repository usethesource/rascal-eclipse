package org.rascalmpl.eclipse.terms;

import org.eclipse.jface.text.IRegion;
import io.usethesource.vallang.IConstructor;
import io.usethesource.vallang.ISet;
import io.usethesource.vallang.IString;
import io.usethesource.vallang.ITuple;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.IWithKeywordParameters;

import io.usethesource.impulse.parser.IParseController;
import io.usethesource.impulse.services.ILanguageSyntaxProperties;

public class TermLanguageSyntaxProperties implements ILanguageSyntaxProperties {
	private IWithKeywordParameters<? extends IConstructor> properties;

	public TermLanguageSyntaxProperties(IConstructor syntaxProperties) {
		this.properties = syntaxProperties.asWithKeywordParameters();
	}

	@Override
	public String getSingleLineCommentPrefix() {
		IValue prop = properties.getParameter("lineComment");
		
		if (prop != null) {
			String val = ((IString) prop).getValue();
			return val.length() != 0 ? val : null;
		}
		
		return null;
	}

	@Override
	public String getBlockCommentStart() {
		IValue prop = properties.getParameter("blockComment");
		return prop != null ? ((IString) ((ITuple) prop).get(0)).getValue() : null;
	}

	@Override
	public String getBlockCommentContinuation() {
		IValue prop = properties.getParameter("blockComment");
		return prop != null ? ((IString) ((ITuple) prop).get(1)).getValue() : null;
	}

	@Override
	public String getBlockCommentEnd() {
		IValue prop = properties.getParameter("blockComment");
		return prop != null ? ((IString) ((ITuple) prop).get(2)).getValue() : null;
	}

	@Override
	public String[][] getFences() {
		IValue prop = properties.getParameter("fences");
		
		if (prop != null) {
			ISet fences = (ISet) prop;
			String[][] result = new String[fences.size()][];
				
			int fenceCount = 0;
			for (IValue elem : fences) {
				ITuple tup = (ITuple) elem;
				result[fenceCount++] = new String[] {((IString) tup.get(0)).getValue(), ((IString) tup.get(1)).getValue()};
			}
			
			return result;
		}
		
		return null;
	}

	@Override
	public String getIdentifierConstituentChars() {
		return null;
	}

	@Override
	public int[] getIdentifierComponents(String ident) {
		return null;
	}

	@Override
	public boolean isIdentifierStart(char ch) {
		return false;
	}

	@Override
	public boolean isIdentifierPart(char ch) {
		return false;
	}

	@Override
	public boolean isWhitespace(char ch) {
		return false;
	}

	@Override
	public IRegion getDoubleClickRegion(int offset, IParseController pc) {
		return null;
	}

}
