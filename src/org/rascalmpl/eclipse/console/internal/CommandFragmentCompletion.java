package org.rascalmpl.eclipse.console.internal;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.imp.utils.Pair;

public class CommandFragmentCompletion {
	private String originalTerm;
	private Iterator<String> suggestions;
	private IInterpreter interpreter;
	
	public CommandFragmentCompletion(IInterpreter interpreter) {
		this.interpreter = interpreter;
	}

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
	private final static Pattern getIdentifier = Pattern.compile(".*?([\\\\]?[_a-zA-Z][\\-_a-zA-Z0-9]*)\\s*$");

	public Pair<Integer, Integer> start(int currentCursorPosition, String currentConsoleInput) {
		int split = findSplitPoint(currentCursorPosition, currentConsoleInput);
		if (split < currentConsoleInput.length()) {
			currentConsoleInput = currentConsoleInput.substring(0, split + 1);
		}
		Matcher m = getIdentifier.matcher(currentConsoleInput);
		if (m.matches()) {
			originalTerm = m.group(1).trim();
			suggestions =  interpreter.findIdentifiers(originalTerm).iterator();
			return new Pair<>(m.start(1), originalTerm.length());
		}
		return new Pair<>(0, 0);
	}
	
	private boolean validRascalIdentifier(char c) {
		return (c >= 'A' && c <= 'Z') 
			|| (c >= 'a' && c <= 'z')
			|| (c >= '0' && c <= '9')
			|| c == '_' || c == '-'
			;
	}

	private int findSplitPoint(int currentCursorPosition, String currentConsoleInput) {
		for (int i = currentCursorPosition; i < currentConsoleInput.length(); i++) {
			if (!validRascalIdentifier(currentConsoleInput.charAt(i)))
				return i - 1;
		}
		return currentConsoleInput.length();
	}

}
