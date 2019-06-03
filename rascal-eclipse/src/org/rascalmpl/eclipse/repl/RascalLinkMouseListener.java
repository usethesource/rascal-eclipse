package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.control.ITerminalMouseListener;
import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.uri.LinkDetector;
import org.rascalmpl.uri.LinkDetector.Type;
import org.rascalmpl.uri.URIResolverRegistry;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.vallang.ISourceLocation;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.exceptions.FactParseError;
import io.usethesource.vallang.exceptions.FactTypeUseException;
import io.usethesource.vallang.io.StandardTextReader;

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
                        if (URIResolverRegistry.getInstance().exists(((ISourceLocation) loc).top())) {
                            EditorUtil.openAndSelectURI((ISourceLocation)loc);
                        }
                        else {
                            Display.getCurrent().beep();
                        }
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