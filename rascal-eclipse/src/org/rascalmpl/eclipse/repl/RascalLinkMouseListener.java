package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.tm.internal.terminal.control.ITerminalMouseListener;
import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.uri.LinkDetector;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.uri.LinkDetector.Type;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.exceptions.FactParseError;
import org.rascalmpl.value.exceptions.FactTypeUseException;
import org.rascalmpl.value.io.StandardTextReader;
import org.rascalmpl.values.ValueFactoryFactory;

final class RascalLinkMouseListener implements ITerminalMouseListener {
    private int currentLine = -1;
    private int currentColumn = -1;

    private String safeToString(char[] ch) {
        if (ch == null) {
            return "";
        }
        return new String(ch);
    }

    @Override
    public void mouseUp(ITerminalTextDataReadOnly model, int line, int column, int button) {
        if (line == currentLine && column == currentColumn) {
            // concat the line before and after to make sure we can get wrapped lines
            String lineBefore = line > 0 && model.isWrappedLine(line - 1) ? safeToString(model.getChars(line - 1)) : "";
            String lineAfter = model.isWrappedLine(line) ? safeToString(model.getChars(line + 1)) : "";
            String fullLine = lineBefore + safeToString(model.getChars(line)) + lineAfter;

            String link = LinkDetector.findAt(fullLine, lineBefore.length() + column);
            if (link != null && LinkDetector.typeOf(link) == Type.SOURCE_LOCATION) {
                try {
                    IValue loc = new StandardTextReader().read(ValueFactoryFactory.getValueFactory(), new StringReader(link));
                    if (loc instanceof ISourceLocation) {
                        EditorUtil.openAndSelectURI((ISourceLocation)loc);
                    }
                }
                catch (FactTypeUseException | FactParseError | IOException e) {
                }
            }
            else if (link != null && LinkDetector.typeOf(link) == Type.HYPERLINK) {
                EditorUtil.openWebURI(ValueFactoryFactory.getValueFactory().sourceLocation(URIUtil.assumeCorrect(link)));
            }
        }
        currentColumn = -1;
        currentLine = -1;
    }

    @Override
    public void mouseDown(ITerminalTextDataReadOnly model, int line, int column, int button) {
        currentLine = line;
        currentColumn = column;
    }

    @Override
    public void mouseDoubleClick(ITerminalTextDataReadOnly model, int line, int column, int button) {
        // TODO: copy source loc to clipboard
    }

}