package org.rascalmpl.eclipse.views.xterm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jline.Terminal;

public class XtermTerminal implements Terminal {
    private final InputStream input;
    private final OutputStream output;

    public XtermTerminal(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void disableInterruptCharacter() {

    }

    @Override
    public void enableInterruptCharacter() {

    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public String getOutputEncoding() {
        return "UTF-8";
    }

    @Override
    public int getWidth() {
        return 80;
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
        return true;
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
    public void setEchoEnabled(boolean arg0) {

    }

    @Override
    public InputStream wrapInIfNeeded(InputStream arg0) throws IOException {
        return input;
    }

    @Override
    public OutputStream wrapOutIfNeeded(OutputStream arg0) {
        return output;
    }
}
