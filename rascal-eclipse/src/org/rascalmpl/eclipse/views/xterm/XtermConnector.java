package org.rascalmpl.eclipse.views.xterm;

import java.io.InputStream;
import java.io.OutputStream;

public interface XtermConnector {
    public void initialize() throws Exception;

    public void connect(InputStream stdin, OutputStream stdout);

    public void disconnect();

    public boolean isLocalEcho();

    public void setTerminalSize(int newWidth, int newHeight);
}
