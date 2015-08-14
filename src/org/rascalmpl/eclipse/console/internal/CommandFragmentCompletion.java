package org.rascalmpl.eclipse.console.internal;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.imp.utils.Pair;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.utils.StringUtils;
import org.rascalmpl.interpreter.utils.StringUtils.OffsetLengthTerm;

public class CommandFragmentCompletion {
	private String originalTerm;
	private Iterator<String> suggestions;

	public void resetSearch() {
		suggestions = null;
		originalTerm = null;
	}

	public boolean isCompleting() {
		return suggestions != null;
	}

	public String nextCompletion() {
		if (suggestions != null && suggestions.hasNext()) {
			return suggestions.next();
		}
		suggestions = null;
		if (originalTerm == null) {
			throw new RuntimeException("Do not call next completion before calling start.");
		}
		return originalTerm;
	}

	public Pair<Integer, Integer> start(int currentCursorPosition, String currentConsoleInput, IEvaluatorContext eval) {
	  OffsetLengthTerm identifier = StringUtils.findRascalIdentifierAtOffset(currentConsoleInput, currentCursorPosition);
	  if (identifier != null) {
	    originalTerm = identifier.term;
			suggestions = eval.completePartialIdentifier(originalTerm).iterator();
			return new Pair<>(identifier.offset, identifier.length);
		}
		return new Pair<>(0, 0);
	}

}
