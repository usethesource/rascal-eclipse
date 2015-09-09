package org.rascalmpl.eclipse.repl;

import static org.rascalmpl.debug.AbstractInterpreterEventTrigger.newInterpreterEventTrigger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.exceptions.FactParseError;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.io.StandardTextReader;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.tm.internal.terminal.emulator.VT100Emulator;
import org.eclipse.tm.internal.terminal.emulator.VT100TerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;
import org.eclipse.tm.internal.terminal.textcanvas.ITextCanvasModel;
import org.eclipse.tm.internal.terminal.textcanvas.TextCanvas;
import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;
import org.rascalmpl.debug.AbstractInterpreterEventTrigger;
import org.rascalmpl.debug.DebugHandler;
import org.rascalmpl.debug.IRascalEventListener;
import org.rascalmpl.debug.IRascalRuntimeInspection;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.debug.core.model.RascalDebugTarget;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.eclipse.nature.ModuleReloader;
import org.rascalmpl.eclipse.nature.ProjectEvaluatorFactory;
import org.rascalmpl.eclipse.nature.WarningsToPrintWriter;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.result.IRascalResult;
import org.rascalmpl.interpreter.result.Result;
import org.rascalmpl.repl.RascalInterpreterREPL;
import org.rascalmpl.uri.LinkDetector;
import org.rascalmpl.uri.LinkDetector.Type;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.values.ValueFactoryFactory;

import jline.Terminal;
import jline.TerminalFactory;

@SuppressWarnings("restriction")
public class RascalTerminalConnector extends TerminalConnectorImpl {
    private RascalInterpreterREPL shell;
    private REPLPipedInputStream stdIn;
    private OutputStream stdInUI;
    private String project;
    private String module;
    private String mode;
    private ILaunch launch;
    private WarningsToPrintWriter warnings;
    private ModuleReloader reloader;
  
   
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

    private File getHistoryFile() throws IOException {
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
        
        Terminal tm = configure(control);
        
        addMouseHandler(((VT100TerminalControl)control), new LinkMouseListener());

        stdIn = new REPLPipedInputStream();
        stdInUI = new REPLPipedOutputStream(stdIn);

        control.setState(TerminalState.CONNECTING);

        RascalTerminalRegistry.getInstance().register(this);
        
        Thread t = new Thread() {
            public void run() {
                try {
                    shell = new RascalInterpreterREPL(stdIn, control.getRemoteToTerminalOutputStream(), true, true, getHistoryFile(), tm) {
                        private AbstractInterpreterEventTrigger eventTrigger;
                        private DebugHandler debugHandler;
                        
                        @Override
                        protected Evaluator constructEvaluator(Writer stdout, Writer stderr) {
                            IProject ipr = project != null ? ResourcesPlugin.getWorkspace().getRoot().getProject(project) : null;
                            Evaluator eval = ProjectEvaluatorFactory.getInstance().createProjectEvaluator(ipr, stderr, stdout);
                            
                            // TODO: this is a workaround to get access to a launch, but we'd rather
                            // just get it from the terminal's properties
                            launch = RascalTerminalRegistry.getInstance().getLaunch();
                            warnings = new WarningsToPrintWriter(new PrintWriter(stderr));
                            reloader = new ModuleReloader(ipr, eval, warnings);

                            if (debug()) {
                                initializeRascalDebugMode(eval);      
                                connectToEclipseDebugAPI(eval);
                                eventTrigger.fireSuspendByClientRequestEvent();
                            }
                            
                            if (module != null) {
                                eval.doImport(null, module);
                                Result<IValue> mainFunc = eval.getCurrentEnvt().getFrameVariable("main");

                                // do not move this queue before the mainFunc initializer
                                super.queueCommand("import " + module + ";");
                                if (mainFunc != null && mainFunc instanceof ICallableValue) {
                                    super.queueCommand("main()");
                                }
                            }
                            
                            return eval;
                        }
                        
                        @Override
                        protected IRascalResult evalStatement(String statement, String lastLine)
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
                        
                        public char ctrl(char ch) {
                          assert 'A' <= ch && ch <= 'Z'; 
                          return (char)((((int)ch) - 'A') + 1);
                        }

                        @Override
                        public void queueCommand(String command) {
                          super.queueCommand(command);
                          try {
                            // let's flush it
                            stdInUI.write(new byte[]{(byte)ctrl('K'),(byte)ctrl('U'),(byte)'\n'});
                          }
                          catch (IOException e) {
                          }
                        }
                    };
                    
                    control.setState(TerminalState.CONNECTED);
                    shell.run();
                }
                catch (IOException e) {
                    Activator.log("terminal not connected", e);
                } 
                finally {
                    control.setState(TerminalState.CLOSED);
                    reloader.destroy();
                    
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
        Terminal tm = TerminalFactory.get();
        tm.setEchoEnabled(false);
        control.setVT100LineWrapping(false);
        VT100Emulator text = ((VT100TerminalControl)control).getTerminalText();
        text.setCrAfterNewLine(true);
        ((VT100TerminalControl)control).setConnectOnEnterIfClosed(false);
        ((VT100TerminalControl)control).setBufferLineLimit(10_000);
        try {
          control.setEncoding(StandardCharsets.UTF_8.name());
        }
        catch (UnsupportedEncodingException e) {
          throw new RuntimeException("UTF8 not available???", e);
        }
        return tm;
    }


    /**
     * Caveat. We use reflection to get around access restrictions here. An issue
     * has been created to ask for public API to register mouse handlers with the terminal 
     * view.
     */
    private void addMouseHandler(VT100TerminalControl control, final ITerminalMouseListener listener) {
        try {
            Field textCanvasField = control.getClass().getDeclaredField("fCtlText");
            textCanvasField.setAccessible(true);
            final TextCanvas textCanvas = (TextCanvas)textCanvasField.get(control);

            Field modelField = textCanvas.getClass().getDeclaredField("fCellCanvasModel");
            modelField.setAccessible(true);
            final ITextCanvasModel model = (ITextCanvasModel) modelField.get(textCanvas);

            final Method screenPointToCellMethod = textCanvas.getClass().getSuperclass().getDeclaredMethod("screenPointToCell", int.class, int.class);
            screenPointToCellMethod.setAccessible(true);


            textCanvas.addMouseListener(new MouseListener() {

                private Point screenPointToCell(int x, int y) {
                    try {
                        return (Point) screenPointToCellMethod.invoke(textCanvas, x, y);
                    }
                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void mouseUp(MouseEvent e) {
                    Point pt = screenPointToCell(e.x, e.y);
                    if (pt != null) {
                        listener.mouseUp(model.getTerminalText(), pt.y, pt.x);
                    }
                }

                @Override
                public void mouseDown(MouseEvent e) {
                    Point pt = screenPointToCell(e.x, e.y);
                    if (pt != null) {
                        listener.mouseDown(model.getTerminalText(), pt.y, pt.x);
                    }
                }

                @Override
                public void mouseDoubleClick(MouseEvent e) {
                    Point pt = screenPointToCell(e.x, e.y);
                    if (pt != null) {
                        listener.mouseDoubleClick(model.getTerminalText(), pt.y, pt.x);
                    }
                }
            });
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doDisconnect() {
        try {
            super.doDisconnect();
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
    }

    private boolean debug() {
        return "debug".equals(mode);
    }

    private static final class LinkMouseListener implements ITerminalMouseListener {
        private int currentLine = -1;
        private int currentOffset = -1;

        private String safeToString(char[] ch) {
            if (ch == null) {
                return "";
            }
            return new String(ch);
        }

        @Override
        public void mouseUp(ITerminalTextDataReadOnly model, int line, int offset) {
            if (line == currentLine && offset == currentOffset) {
                // concat the line before and after to make sure we can get wrapped lines
                String lineBefore = line > 0 && model.isWrappedLine(line - 1) ? safeToString(model.getChars(line - 1)) : "";
                String lineAfter = model.isWrappedLine(line) ? safeToString(model.getChars(line + 1)) : "";
                String fullLine = lineBefore + safeToString(model.getChars(line)) + lineAfter;

                String link = LinkDetector.findAt(fullLine, lineBefore.length() + offset);
                if (link != null && LinkDetector.typeOf(link) == Type.SOURCE_LOCATION) {
                    try {
                        IValue loc = new StandardTextReader().read(ValueFactoryFactory.getValueFactory(), new StringReader(link));
                        if (loc instanceof ISourceLocation) {
                            EditorUtil.openAndSelectURI((ISourceLocation)loc);
                        }
                    }
                    catch (FactTypeUseException | FactParseError | IOException e) {
                    }
                }
                else if (link != null && LinkDetector.typeOf(link) == Type.HYPERLINK) {
                    EditorUtil.openWebURI(ValueFactoryFactory.getValueFactory().sourceLocation(URIUtil.assumeCorrect(link)));
                }
            }
            offset = -1;
            currentLine = -1;
        }

        @Override
        public void mouseDown(ITerminalTextDataReadOnly model, int line, int offset) {
            currentLine = line;
            currentOffset = offset;
        }

        @Override
        public void mouseDoubleClick(ITerminalTextDataReadOnly model, int line, int offset) {
            // TODO: copy source loc to clipboard
        }
    }

}
