package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.tm.internal.terminal.emulator.VT100TerminalControl;

import jline.Terminal;

@SuppressWarnings("restriction")
public class TMTerminalTerminal implements Terminal {
    private final VT100TerminalControl ctx;
    private SizedTerminalConnector rascalTerminalConnector;

    public TMTerminalTerminal(VT100TerminalControl ctx, SizedTerminalConnector rascalTerminalConnector) {
        this.ctx = ctx;
        this.rascalTerminalConnector = rascalTerminalConnector;
    }

    @Override
    public int getHeight() {
        return rascalTerminalConnector.getHeight() ;
    }

    @Override
    public String getOutputEncoding() {
        return ctx.getEncoding();
    }

    @Override
    public int getWidth() {
        return rascalTerminalConnector.getWidth() ;
    }

    @Override
    public boolean hasWeirdWrap() {
        return false;
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public boolean isAnsiSupported() {
        return true;
    }

    @Override
    public boolean isEchoEnabled() {
        return false;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public void reset() throws Exception {
    }

    @Override
    public void restore() throws Exception {
    }

    @Override
    public void setEchoEnabled(boolean enabled) {
        if (enabled) {
            throw new RuntimeException("Echo should not be enabled");
        }
    }

    @Override
    public InputStream wrapInIfNeeded(InputStream in) throws IOException {
        return in;
    }

    @Override
    public OutputStream wrapOutIfNeeded(OutputStream out) {
        return out;
    }
    
    
    @Override
    public void enableInterruptCharacter() {
        // TODO: figure out if we need to do something here
    }
    
    @Override
    public void disableInterruptCharacter() {
        // TODO: figure out if we need to do something here
    }

}
