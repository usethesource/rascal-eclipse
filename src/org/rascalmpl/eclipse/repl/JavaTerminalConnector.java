package org.rascalmpl.eclipse.repl;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.tm.internal.terminal.emulator.VT100Emulator;
import org.eclipse.tm.internal.terminal.emulator.VT100TerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;
import org.rascalmpl.eclipse.Activator;

import jline.Terminal;
import jline.TerminalFactory;

@SuppressWarnings("restriction")
public class JavaTerminalConnector extends TerminalConnectorImpl {

    @Override
    public OutputStream getTerminalToRemoteStream() {
        return stdInUI;
    }

    private REPLPipedInputStream stdIn;
    private OutputStream stdInUI;
    private String file;
    private ILaunchConfiguration config;
    
    @Override
    public boolean isLocalEcho() {
        return false;
    }

    @Override
    public void load(ISettingsStore store) {
        String label = store.get("launchConfiguration");
        
        if (label != null) {
            for (ILaunchConfiguration config : JavaLauncherDelegate.getJavaLaunchConfigs()) {
                if (config.getName().equals(label)) {
                    this.config = config;
                }
            }
        }
    }

    @Override
    public void connect(ITerminalControl control) {
        super.connect(control);
        Terminal tm = configure(control);
        
        stdIn = new REPLPipedInputStream();
        stdInUI = new REPLPipedOutputStream(stdIn);

        control.setState(TerminalState.CONNECTING);
        
        try {
            // create a new configuration for the rascal file
            ILaunchConfigurationWorkingCopy workingCopy = config.getWorkingCopy();
            ILaunchConfiguration configuration = workingCopy.doSave();
            DebugUITools.launch(configuration, "run");
            control.setState(TerminalState.CONNECTED);
        } catch (CoreException e1) {
            Activator.log(e1.getMessage(), e1);
            control.setState(TerminalState.CLOSED);
        }
    }

    public ITerminalControl getControl() {
        return fControl;
    }

    private Terminal configure(ITerminalControl control) {
        Terminal tm = TerminalFactory.get();
        tm.setEchoEnabled(false);
        control.setVT100LineWrapping(false);
        VT100Emulator text = ((VT100TerminalControl)control).getTerminalText();
        text.setCrAfterNewLine(true);
        ((VT100TerminalControl)control).setBufferLineLimit(10_000);
        try {
          control.setEncoding(StandardCharsets.UTF_8.name());
        }
        catch (UnsupportedEncodingException e) {
          throw new RuntimeException("UTF8 not available???", e);
        }
        return tm;
    }


    @Override
    protected void doDisconnect() {
        super.doDisconnect();
    }

    public void setFocus() {
        ((VT100TerminalControl) fControl).setFocus();
    }
    
    @Override
    public String getSettingsSummary() {
        return file != null ? "Running Java program " + file : "no file associated";
    }
}
