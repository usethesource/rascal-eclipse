package org.rascalmpl.eclipse.repl;

import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

public abstract class SizedTerminalConnector extends TerminalConnectorImpl {
    public abstract int getWidth();
    public abstract int getHeight();
}
