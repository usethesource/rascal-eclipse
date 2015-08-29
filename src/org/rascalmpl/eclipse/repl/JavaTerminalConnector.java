package org.rascalmpl.eclipse.repl;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.IStructuredSelection;
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
   public JavaTerminalConnector() {
       System.err.println("Jello");
   }

    private REPLPipedInputStream stdIn;
    private OutputStream stdInUI;
    private String file;

    @Override
    public boolean isLocalEcho() {
        return false;
    }

    @Override
    public void load(ISettingsStore store) {
        this.file = store.get("file");
    }

    @Override
    public void connect(ITerminalControl control) {
        super.connect(control);
        Terminal tm = configure(control);
        
        stdIn = new REPLPipedInputStream();
        stdInUI = new REPLPipedOutputStream(stdIn);

        control.setState(TerminalState.CONNECTING);
        
        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType type = launchManager.getLaunchConfigurationType("rascal-eclipse.java.terminal");
        
        // this code can find existing launch configs.
        
//      try {
//      ILaunchConfiguration[] configurations = launchManager.getLaunchConfigurations(type);
//      for (int i = 0; i < configurations.length; i++) {
//          ILaunchConfiguration configuration = configurations[i];
//          String attribute = configuration.getAttribute("class", (String)null);
//          if (resourceFullPath.equals(attribute)) {
//              DebugUITools.launch(configuration, mode);
//              return;
//          }
//      }
//  } catch (CoreException e) {
//      IStatus message = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
//      Activator.getInstance().getLog().log(message);
//      return;
//  }
//  
        try {
            // create a new configuration for the rascal file
            ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null,  file);
            workingCopy.setAttribute("class", file);
            ILaunchConfiguration configuration = workingCopy.doSave();
            configuration.getAttributes().put("connector", this);
            DebugUITools.launch(configuration, "run");
        } catch (CoreException e1) {
            IStatus message = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e1.getMessage(), e1);
            Activator.getInstance().getLog().log(message);
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
        ((VT100TerminalControl)fControl).setFocus();
    }
    
    @Override
    public String getSettingsSummary() {
        return file != null ? "Running Java program " + file : "no file associated";
    }
}
