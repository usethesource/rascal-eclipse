/**
 * 
 */
package org.meta_environment.rascal.eclipse.console;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
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
		int linkOffset = event.getOffset();
		int linkLength = event.getLength() - 1;
		IDocument doc = console.getDocument();

		try {
			String match = doc.get(linkOffset, linkLength);
			String[] filePosSplit = match.split(":");
			String file = filePosSplit[0];
			String[] lineColSplit = filePosSplit[1].split(",");
			int line = Integer.parseInt(lineColSplit[0]);
			int col = Integer.parseInt(lineColSplit[1]);
			
			console.addHyperlink(new RascalErrorHyperLink(file, line, col), linkOffset, linkLength);
		} catch (BadLocationException e) {
			Activator.getInstance().logException("hyperlink", e);			
		} catch (NumberFormatException e) {
			Activator.getInstance().logException("hyperlink", e);
		}
	}
}