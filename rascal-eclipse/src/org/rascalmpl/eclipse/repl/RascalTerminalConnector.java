package org.rascalmpl.eclipse.repl;

import static org.rascalmpl.debug.AbstractInterpreterEventTrigger.newInterpreterEventTrigger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.tm.internal.terminal.emulator.VT100Emulator;
import org.eclipse.tm.internal.terminal.emulator.VT100TerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.eclipse.ui.progress.UIJob;
import org.rascalmpl.debug.AbstractInterpreterEventTrigger;
import org.rascalmpl.debug.DebugHandler;
import org.rascalmpl.debug.IRascalEventListener;
import org.rascalmpl.debug.IRascalRuntimeInspection;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.debug.core.model.RascalDebugTarget;
import org.rascalmpl.eclipse.nature.IWarningHandler;
import org.rascalmpl.eclipse.nature.ModuleReloader;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.nature.RascalMonitor;
import org.rascalmpl.eclipse.nature.WarningsToPrintWriter;
import org.rascalmpl.eclipse.util.ThreadSafeImpulseConsole;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.result.IRascalResult;
import org.rascalmpl.library.util.SemVer;
import org.rascalmpl.repl.BaseREPL;
import org.rascalmpl.repl.BaseRascalREPL;
import org.rascalmpl.repl.RascalInterpreterREPL;
import org.rascalmpl.shell.RascalShell;

import jline.Terminal;

@SuppressWarnings("restriction")
public class RascalTerminalConnector extends SizedTerminalConnector {
    private BaseREPL shell;
    private final AtomicBoolean shellIsRunning = new AtomicBoolean(false);
    private REPLPipedInputStream stdIn;
    private OutputStream stdInUI;
    protected String project;
    protected String module;
    protected String mode;
    private ILaunch launch;
    private IWarningHandler warnings;
    private ModuleReloader reloader;
    private int terminalHeight = 24;
    private int terminalWidth = 80;
  
    @Override
    public OutputStream getTerminalToRemoteStream() {
        return stdInUI;
    }

	@Override
    public boolean isLocalEcho() {
        return false;
    }

    @Override
    public void load(ISettingsStore store) {
        this.project = store.get("project");
        this.module = store.get("module");
        this.mode = store.get("mode");
    }

    protected File getHistoryFile() throws IOException {
        File home = new File(System.getProperty("user.home"));
        File rascal = new File(home, ".rascal");
        if (!rascal.exists()) {
            rascal.mkdirs();
        }
        File historyFile = new File(rascal, ".repl-history-rascal");
        if (!historyFile.exists()) {
            historyFile.createNewFile();
        }
        return historyFile;
    }

    @Override
    public void connect(ITerminalControl control) {
        super.connect(control);
        
        final Terminal tm = configure(control);
        

        stdIn = new REPLPipedInputStream();
        stdInUI = new REPLPipedOutputStream(stdIn);

        control.setState(TerminalState.CONNECTING);

        RascalTerminalRegistry.getInstance().register(this);
        
        Thread t = new Thread() {
            public void run() {
                try {
                    shell = constructREPL(control, stdIn, control.getRemoteToTerminalOutputStream(), tm);
                    control.setState(TerminalState.CONNECTED);

                    if (module != null) {
                        queueCommand("import " + module + ";");
                        queueCommand("main()");
                    }
                    
                    String version = RascalShell.getVersionNumber();
                    shell.getOutput().println("Rascal Version: " + version);
                    if (new SemVer(version).getPrerelease().equals("SNAPSHOT")) {
                        shell.getOutput().print(
                                "Rascal's daily SNAPSHOT releases have become more unstable recently.\n" +
                                "We recommend switching to the (monthly) stable release strain 0.16.x from https://update.rascal-mpl.org/stable as soon as possible.\n" +
                                "Until the end of Feb 2020, the stable release will have a higher version than the daily unstable, to facilitate your move to stable.\n\n");
                        
                    }
                
                    shellIsRunning.set(true);
                    shell.run();
                }
                catch (IOException | URISyntaxException e) {
                    Activator.log("terminal not connected", e);
                } 
                finally {
                    control.setState(TerminalState.CLOSED);
                    
                    if (reloader != null) {
                        reloader.destroy();
                    }
                    
                    try {
                        if (debug()) {
                            launch.getDebugTarget().terminate();
                            launch.removeDebugTarget(launch.getDebugTarget());
                        }
                        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
                        launchManager.removeLaunch(launch);
                    } catch (DebugException e) {
                        Activator.log("problem disconnecting from debugger", e);
                    }
                }
            }
        };
        t.setName("Rascal REPL Runner");
        t.start();

    }


    private Terminal configure(ITerminalControl control) {
        VT100TerminalControl vtControl = (VT100TerminalControl) control;
        Terminal tm = new TMTerminalTerminal(vtControl, this);
        vtControl.setVT100LineWrapping(false);
        VT100Emulator text = vtControl.getTerminalText();
        text.setCrAfterNewLine(true);
        vtControl.setConnectOnEnterIfClosed(false);
        vtControl.setBufferLineLimit(10_000);
        try {
          control.setEncoding(StandardCharsets.UTF_8.name());
        }
        catch (UnsupportedEncodingException e) {
          throw new RuntimeException("UTF8 not available???", e);
        }
        vtControl.addMouseListener(new RascalLinkMouseListener());
        return tm;
    }
    

    @Override
    protected void doDisconnect() {
        try {
            super.doDisconnect();
            if (shell != null) {
                stdIn.close();
                shell.stop();
                shell = null;
            }
        } finally {
            RascalTerminalRegistry.getInstance().unregister(this);
        }
    }

    public void setFocus() {
        ((VT100TerminalControl)fControl).setFocus();
        RascalTerminalRegistry.getInstance().setActive(this);
    }
    
    @Override
    public String getSettingsSummary() {
        return project != null ? "Rascal Terminal [project: " + project + "]" : "Rascal Terminal [no project]";
    }

    public String getProject() {
        return project;
    }

    public void queueCommand(String cmd) {
        shell.queueCommand(cmd);
        if (shellIsRunning.get()) {
            try {
                // let's flush it.
                stdInUI.write(new byte[]{(byte)ctrl('K'),(byte)ctrl('U'),(byte)'\n'});
            }
            catch (IOException e) {
                // tough, but we don't have a sensible way of dealing with this,
                // and if it does happen, the user will have bigger issues than the current one.
            }
        }
    }
    
    private static char ctrl(char ch) {
        assert 'A' <= ch && ch <= '_'; 
        return (char)((((int)ch) - 'A') + 1);
    }

    private boolean debug() {
        return "debug".equals(mode);
    }

    @Override
    public void setTerminalSize(int newWidth, int newHeight) {
        super.setTerminalSize(newWidth, newHeight);
        terminalHeight = newHeight;
        terminalWidth = newWidth;
    }

    public int getHeight() {
        return terminalHeight;
    }

    public int getWidth() {
        return terminalWidth;
    }

    protected BaseREPL constructREPL(ITerminalControl control, REPLPipedInputStream stdIn, OutputStream stdInUI, Terminal tm) throws IOException, URISyntaxException {
        BaseRascalREPL repl = constructRascalREPL(control, stdIn, stdInUI, tm);
        return new BaseREPL(repl, null, stdIn, stdInUI, true, true, getHistoryFile(), tm, new EclipseIDEServices());
    }
    
    protected BaseRascalREPL constructRascalREPL(ITerminalControl control, REPLPipedInputStream stdIn, OutputStream stdInUI, Terminal tm) throws IOException, URISyntaxException {
        return new RascalInterpreterREPL(stdIn, control.getRemoteToTerminalOutputStream(), true, true, false, getHistoryFile()) {
            private AbstractInterpreterEventTrigger eventTrigger;
            private DebugHandler debugHandler;
            
            @Override
            protected Evaluator constructEvaluator(Writer stdout, Writer stderr) {
                IProject ipr = project != null ? ResourcesPlugin.getWorkspace().getRoot().getProject(project) : null;
                if (ipr != null && !ipr.isOpen()) {
                    ipr = null;
                }
                Evaluator eval = ProjectEvaluatorFactory.getInstance().createProjectEvaluator(ipr, stderr, stdout);
                
                // TODO: this is a workaround to get access to a launch, but we'd rather
                // just get it from the terminal's properties
                launch = RascalTerminalRegistry.getInstance().getLaunch();
                warnings = new WarningsToPrintWriter(new PrintWriter(ThreadSafeImpulseConsole.INSTANCE.getWriter()));
                reloader = new ModuleReloader(ipr, eval, warnings);
                eval.setMonitor(new RascalMonitor(new NullProgressMonitor(), warnings));

                if (debug()) {
                    initializeRascalDebugMode(eval);      
                    connectToEclipseDebugAPI(eval);
                    eventTrigger.fireSuspendByClientRequestEvent();
                }
                
                return eval;
            }
            
            @Override
            public void handleInput(String line, Map<String, InputStream> output, Map<String, String> metadata)
            		throws InterruptedException {
            	super.handleInput(line, output, metadata);
            	
            	for (String mimetype : output.keySet()) {
                    if (!mimetype.contains("html") && !mimetype.startsWith("image/")) {
                        continue;
                    }

            		new UIJob("Content") {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							try {
								String id = metadata.get("url");
								URL url = new URL(id);
								IWebBrowser browser = WorkbenchBrowserSupport.getInstance().createBrowser(IWorkbenchBrowserSupport.AS_EDITOR, id, id, "This browser shows the latest web content produced by a Rascal terminal");
								browser.openURL(url);
							} catch (PartInitException | MalformedURLException e) {
								Activator.log("could not view HTML content", e);
							}
							
							return Status.OK_STATUS;
						}
					}.schedule();
                }
            }
            
            @Override
            public IRascalResult evalStatement(String statement, String lastLine)
                    throws InterruptedException {
                try {
                    if (debug()) {
                        synchronized(eval) {
                            eventTrigger.fireResumeByClientRequestEvent();
                        }
                    }
                    
                    Job job = new Job("Reloading modules") {
                        @Override
                        protected IStatus run(IProgressMonitor monitor) {
                            reloader.updateModules(monitor, warnings, Collections.emptySet());
                            return Status.OK_STATUS;
                        }
                    };
                    job.schedule();
                    job.join();
                 
                    return super.evalStatement(statement, lastLine);
                }
                finally {
                    if (debug() && !":quit".equals(statement.trim())) {
                        synchronized(eval) {
                            eventTrigger.fireSuspendByClientRequestEvent();
                        }
                    }
                }
            }
            
            private void connectToEclipseDebugAPI(IRascalRuntimeInspection eval) {
                try {
                    RascalDebugTarget  debugTarget = new RascalDebugTarget(eval, launch, eventTrigger, debugHandler);
                    launch.addDebugTarget(debugTarget);
                    debugTarget.breakpointManagerEnablementChanged(true);
                } catch (CoreException e) {
                    Activator.log("could not connect to debugger", e);
                    // otherwise no harm done, can continue
                }
             
            }
            
            private void initializeRascalDebugMode(Evaluator eval) {
                eventTrigger = newInterpreterEventTrigger(this, new CopyOnWriteArrayList<IRascalEventListener>());
                debugHandler = new DebugHandler();
                debugHandler.setEventTrigger(eventTrigger);
                debugHandler.setTerminateAction(new Runnable() {
                    @Override
                    public void run() {
                        doDisconnect();
                    }
                });         
                eval.addSuspendTriggerListener(debugHandler);
            }
        };
    }
}
