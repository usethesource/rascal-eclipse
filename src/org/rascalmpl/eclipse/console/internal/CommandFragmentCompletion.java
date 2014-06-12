package org.rascalmpl.eclipse.console.internal;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private final static Pattern getIdentifier = Pattern.compile(".*?([_a-zA-Z][_a-zA-Z0-9]*)\\s*$");

	public int start(String currentConsoleInput) {
		Matcher m = getIdentifier.matcher(currentConsoleInput);
		if (m.matches()) {
			originalTerm = m.group(1).trim();
			suggestions =  interpreter.findIdentifiers(originalTerm).iterator();
			return m.start(1);
		}
		return 0;
	}
}
