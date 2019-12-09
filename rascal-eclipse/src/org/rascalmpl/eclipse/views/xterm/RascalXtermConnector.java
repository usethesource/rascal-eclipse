package org.rascalmpl.eclipse.views.xterm;

import static org.rascalmpl.debug.AbstractInterpreterEventTrigger.newInterpreterEventTrigger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.rascalmpl.repl.BaseREPL;
import org.rascalmpl.repl.BaseRascalREPL;
import org.rascalmpl.repl.RascalInterpreterREPL;
import org.rascalmpl.shell.RascalShell;

import jline.Terminal;

@SuppressWarnings("restriction")
public class RascalXtermConnector implements XtermConnector {
    private BaseREPL shell;
    private final AtomicBoolean shellIsRunning = new AtomicBoolean(false);
    private XtermPipedInputStream stdIn;
    private OutputStream stdInUI;
    protected String project = "rascal";
    protected String module = null;
    protected String mode = "run";
    private ILaunch launch;
    private IWarningHandler warnings;
    private ModuleReloader reloader;
    private int terminalHeight = 24;
    private int terminalWidth = 80;
    private XtermTerminal tm;
    private XtermView view;
  
    @Override
    public OutputStream getTerminalToRemoteStream() {
        return stdInUI;
    }

	@Override
    public boolean isLocalEcho() {
        return false;
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
    public void connect(XtermServer control, XtermView view) {
        stdIn = new XtermPipedInputStream();
        stdInUI = new XTermPipedOutputStream(stdIn);

        RascalXtermRegistry.getInstance().register(this);
        
        Thread t = new Thread() {
            public void run() {
                try {
                    shell = constructREPL(control, stdIn, control.getRemoteToTerminalOutputStream(), configure(control));

                    if (module != null) {
                        queueCommand("import " + module + ";");
                        queueCommand("main()");
                    }
                    
                    String version = RascalShell.getVersionNumber();
                    shell.getOutput().println("Rascal Version: " + version);
                
                    shellIsRunning.set(true);
                    shell.run();
                }
                catch (IOException | URISyntaxException e) {
                    Activator.log("terminal not connected", e);
                } 
                finally {
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


    private Terminal configure(XtermServer control) {
        if (tm == null) {
            tm = new XtermTerminal(control, this);
        }
        return tm;
    }
    
    public void setFocus() {
        view.setFocus();
        RascalXtermRegistry.getInstance().setActive(this);
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
        terminalHeight = newHeight;
        terminalWidth = newWidth;
    }

    public int getHeight() {
        return terminalHeight;
    }

    public int getWidth() {
        return terminalWidth;
    }

    protected BaseREPL constructREPL(XtermServer control, XtermPipedInputStream stdIn, OutputStream stdInUI, Terminal tm) throws IOException, URISyntaxException {
        BaseRascalREPL repl = constructRascalREPL(control, stdIn, stdInUI, tm);
        return new BaseREPL(repl, null, stdIn, stdInUI, true, true, getHistoryFile(), tm, new XtermIDEServices());
    }
    
    protected BaseRascalREPL constructRascalREPL(XtermServer control, XtermPipedInputStream stdIn, OutputStream stdInUI, Terminal tm) throws IOException, URISyntaxException {
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
                launch = RascalXtermRegistry.getInstance().getLaunch();
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
                        disconnect();
                    }
                });         
                eval.addSuspendTriggerListener(debugHandler);
            }
        };
    }

    @Override
    public void initialize() throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void disconnect() {
        try {
            if (shell != null) {
                try {
                    stdIn.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                shell.stop();
                shell = null;
            }
        } finally {
            RascalXtermRegistry.getInstance().unregister(this);
        }        
    }
}
