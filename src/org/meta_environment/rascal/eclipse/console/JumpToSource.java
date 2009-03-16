/**
 * 
 */
package org.meta_environment.rascal.eclipse.console;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.meta_environment.rascal.eclipse.Activator;

public class JumpToSource implements IPatternMatchListener {
	private TextConsole console;

	@Override
	public int getCompilerFlags() {
		return 0;
	}

	@Override
	public String getLineQualifier() {
		return null;
	}

	@Override
	public String getPattern() {
		return ".*\\.rsc:[0-9]*,[0-9]*:";
	}

	@Override
	public void connect(TextConsole console) {
		this.console = console;
	}

	@Override
	public void disconnect() {
	}

	@Override
	public void matchFound(PatternMatchEvent event) {
		final int offset = event.getOffset();
		final int len = event.getLength();
		try {
			console.addHyperlink(new RascalErrorHyperLink(console.getDocument(), offset, len), offset, len - 1);
		} catch (BadLocationException e) {
			Activator.getInstance().logException("hyperlink", e);			
		}
	}
}