/**
 * 
 */
package org.meta_environment.rascal.eclipse.console;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.meta_environment.rascal.eclipse.Activator;
import org.meta_environment.rascal.eclipse.console.ConsoleFactory.IRascalConsole;

public class JumpToSource implements IPatternMatchListener {
	private IRascalConsole console;

	public int getCompilerFlags() {
		return 0;
	}

	public String getLineQualifier() {
		return null;
	}

	public String getPattern() {
		return ".*\\.rsc:[0-9]*,[0-9]*:";
	}

	public void connect(TextConsole console) {
		// can be only connected to instances of IRascalConsole
		if (console instanceof IRascalConsole) {
			this.console = (IRascalConsole) console;
		} else {
			throw new RuntimeException("unable to connect to the console "+console);
		}
	}

	public void disconnect() {
	}

	public void matchFound(PatternMatchEvent event) {
		int linkOffset = event.getOffset();
		int linkLength = event.getLength() - 1;
		IDocument doc = console.getDocument();

		try {
			String match = doc.get(linkOffset, linkLength).trim();
			String[] filePosSplit = match.split(":");
			String filename = filePosSplit[0];
			String[] lineColSplit = filePosSplit[1].split(",");
			int line = Integer.parseInt(lineColSplit[0]);
			int col = Integer.parseInt(lineColSplit[1]);

			try {
				IFile file = console.getRascalInterpreter().getFile(filename);
				console.addHyperlink(new RascalErrorHyperLink(file, line, col), linkOffset, linkLength);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (BadLocationException e) {
			Activator.getInstance().logException("hyperlink", e);			
		} catch (NumberFormatException e) {
			Activator.getInstance().logException("hyperlink", e);
		}
	}
}