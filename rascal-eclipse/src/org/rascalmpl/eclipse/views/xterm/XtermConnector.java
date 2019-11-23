package org.rascalmpl.eclipse.views.xterm;

import java.io.OutputStream;

public interface XtermConnector {
    public void initialize() throws Exception;

    public void connect(XtermServer server, XtermView viewer);

    public void disconnect();

    public OutputStream getTerminalToRemoteStream();

    public boolean isLocalEcho();

    public void setTerminalSize(int newWidth, int newHeight);
}
