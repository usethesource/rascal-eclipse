package org.rascalmpl.eclipse.views.xterm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jline.Terminal;

public class XtermTerminal implements Terminal {
    private XtermServer server;
    private RascalXtermConnector connector;

    public XtermTerminal(XtermServer control, RascalXtermConnector connector) {
        this.server = control;
        this.connector = connector;
    }

    @Override
    public void disableInterruptCharacter() {
        // TODO Auto-generated method stub

    }

    @Override
    public void enableInterruptCharacter() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getHeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getOutputEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getWidth() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasWeirdWrap() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void init() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isAnsiSupported() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEchoEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSupported() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void reset() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void restore() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void setEchoEnabled(boolean arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public InputStream wrapInIfNeeded(InputStream arg0) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream wrapOutIfNeeded(OutputStream arg0) {
        return server.getRemoteToTerminalOutputStream();
    }
}
