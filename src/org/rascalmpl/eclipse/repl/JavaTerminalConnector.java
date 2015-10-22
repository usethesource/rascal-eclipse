package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.terminal.emulator.VT100TerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;
import org.rascalmpl.eclipse.Activator;

@SuppressWarnings("restriction")
public class JavaTerminalConnector extends TerminalConnectorImpl {

    @Override
    public OutputStream getTerminalToRemoteStream() {
        return stdInUI;
    }

    private OutputStream stdInUI;
    private String file;
    private ILaunchConfiguration config;
    private String mode = "run";
    private ILaunch launch;
    private IDebugEventSetListener detectTerminated;
    
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
        
        if (this.config == null) {
        	throw new RuntimeException("unable to load configuration " + label);
        }
        
        mode = store.get("mode");
    }

    @Override
    public void connect(ITerminalControl control) {
    	assert this.config != null;
    	
        super.connect(control);
        configure((VT100TerminalControl)control);
        control.setState(TerminalState.CONNECTING);

        try {
          ILaunchConfigurationWorkingCopy workingCopy = config.getWorkingCopy();
          
          // this is necessary to enable the test for ATTR_CAPTURE_IN_FILE:
          workingCopy.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_FILE, System.getProperty("os.name").startsWith("Windows") ? "nul" : "/dev/null");
          
          // this makes sure the terminal does not echo the characters to the normal console as well:
          workingCopy.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, false);
          
          ILaunchConfiguration configuration = workingCopy.doSave();
          
          launch = configuration.launch(mode, new NullProgressMonitor(), true /*build first*/, true /*do register for debug*/);

          if (launch.getProcesses().length == 1) {
            final IProcess currentProcess = launch.getProcesses()[0];

            final IStreamsProxy proxy = currentProcess.getStreamsProxy();
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
                private boolean firstPrint = true;
                
              @Override
              public void streamAppended(String text, IStreamMonitor monitor) {
                try {
                    if (firstPrint) {
                        Display.getDefault().asyncExec(new Runnable() {
                            public void run() {
                                setFocus();
                            };
                        });
                        
                        firstPrint = false;
                    }
                  control.getRemoteToTerminalOutputStream().write(text.getBytes(StandardCharsets.UTF_8));
                }
                catch (IOException e) {
                }
              }
            });
            proxy.getErrorStreamMonitor().addListener(new IStreamListener() {
              @Override
              public void streamAppended(String text, IStreamMonitor monitor) {
                try {
                  control.getRemoteToTerminalOutputStream().write(text.getBytes(StandardCharsets.UTF_8));
                }
                catch (IOException e) {
                }
              }
            });
            detectTerminated = new IDebugEventSetListener() {
              @Override
              public void handleDebugEvents(DebugEvent[] events) {
                for (int i = 0; i < events.length; i++) {
                  if (events[i].getSource() == currentProcess && events[i].getKind() == DebugEvent.TERMINATE) {
                    control.setState(TerminalState.CLOSED);
                    DebugPlugin.getDefault().removeDebugEventListener(detectTerminated);
                    break;
                  }
                }
              }
            };
            DebugPlugin.getDefault().addDebugEventListener(detectTerminated);
            
            control.setState(TerminalState.CONNECTED);
            setFocus();
          }
          else {
            control.setState(TerminalState.CLOSED);
          }
        } 
        catch (Throwable e1) {
          Activator.log(e1.getMessage(), e1);
          control.setState(TerminalState.CLOSED);
        }
    }

    public ITerminalControl getControl() {
        return fControl;
    }

    private void configure(VT100TerminalControl control) {
        control.setConnectOnEnterIfClosed(false);
        control.setVT100LineWrapping(false);
        control.setBufferLineLimit(10_000);
        try {
          control.setEncoding(StandardCharsets.UTF_8.name());
        }
        catch (UnsupportedEncodingException e) {
          throw new RuntimeException("UTF8 not available???", e);
        }
        control.getTerminalText().setCrAfterNewLine(true);
        RascalTerminalConnector.addRascalLinkMouseHandler(control);
    }


    @Override
    protected void doDisconnect() {
        super.doDisconnect();
        if (launch != null) {
          try {
            launch.terminate();
          }
          catch (DebugException e) {
            Activator.log(e.getMessage(), e);
          }
        }
    }

    public void setFocus() {
        ((VT100TerminalControl) fControl).setFocus();
    }
    
    @Override
    public String getSettingsSummary() {
        return file != null ? "Running Java program " + file : "no file associated";
    }
    
   

}
