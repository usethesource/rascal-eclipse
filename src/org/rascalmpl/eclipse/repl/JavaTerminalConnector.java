package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
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

        control.setState(TerminalState.CONNECTING);
        
        try {
            // create a new configuration for the rascal file
            ILaunchConfigurationWorkingCopy workingCopy = config.getWorkingCopy();
            ILaunchConfiguration configuration = workingCopy.doSave();
            ILaunch launch = configuration.launch("run", new NullProgressMonitor(), true /*build first*/);
            

            if (launch.getProcesses().length == 1) {
              final IStreamsProxy proxy = launch.getProcesses()[0].getStreamsProxy();
              stdInUI = new OutputStream() {
                //private final CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
                @Override
                public void write(int b) throws IOException {
                  // todo handle multi byte utf8 stuff (anything not ASCII breaks here)
                  proxy.write(new String(new byte[]{(byte)b}, StandardCharsets.UTF_8));
                }
                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                  proxy.write(new String(b, off, len, StandardCharsets.UTF_8));
                }
                @Override
                public void write(byte[] b) throws IOException {
                  write(b, 0, b.length);
                }

              };
              proxy.getOutputStreamMonitor().addListener(new IStreamListener() {
                
                @Override
                public void streamAppended(String text, IStreamMonitor monitor) {
                  try {
                    control.getRemoteToTerminalOutputStream().write(text.getBytes(StandardCharsets.UTF_8));
                  }
                  catch (IOException e) {
                    e.printStackTrace();
                  }
                }
              });
              
            }
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
