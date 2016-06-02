package org.rascalmpl.eclipse.repl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.tm.internal.terminal.emulator.VT100Emulator;
import org.eclipse.tm.internal.terminal.emulator.VT100TerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.textcanvas.ITextCanvasModel;
import org.eclipse.tm.internal.terminal.textcanvas.TextCanvas;
import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.editor.EditorUtil;
import org.rascalmpl.eclipse.util.ProjectConfig;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.repl.CommandExecutor;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.repl.CompiledRascalREPL;
import org.rascalmpl.uri.LinkDetector;
import org.rascalmpl.uri.LinkDetector.Type;
import org.rascalmpl.uri.URIUtil;
import org.rascalmpl.value.ISourceLocation;
import org.rascalmpl.value.IValue;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.value.exceptions.FactParseError;
import org.rascalmpl.value.exceptions.FactTypeUseException;
import org.rascalmpl.value.io.StandardTextReader;
import org.rascalmpl.values.ValueFactoryFactory;

import jline.Terminal;

@SuppressWarnings("restriction")
public class CompiledRascalTerminalConnector extends SizedTerminalConnector {
    private static final IValueFactory vf = ValueFactoryFactory.getValueFactory();
    private CompiledRascalREPL shell;
    private REPLPipedInputStream stdIn;
    private OutputStream stdInUI;
    private String project;
    private String mode;
    private ILaunch launch;
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
//        this.module = store.get("module");
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

        stdIn = new REPLPipedInputStream();
        stdInUI = new REPLPipedOutputStream(stdIn);

        control.setState(TerminalState.CONNECTING);

//        RascalTerminalRegistry.getInstance().register(this);
        
        Thread t = new Thread() {
            public void run() {
                try {
                    IProject ipr = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
                    
                    if (ipr == null) {
                        Activator.log("No project selected to configure console for", new NullPointerException());
                        return;
                    }
                    
                    shell = new CompiledRascalREPL(stdIn, control.getRemoteToTerminalOutputStream(), true, true, getHistoryFile(), tm, new ProjectConfig(vf).getPathConfig(ipr)) {
                        
                        @Override
                        protected CommandExecutor constructCommandExecutor(PrintWriter stdout, PrintWriter stderr) throws IOException ,org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.NoSuchRascalFunction ,URISyntaxException {
                            return new CommandExecutor(stdout, stderr);
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
                catch (IOException | URISyntaxException e) {
                    Activator.log("terminal not connected", e);
                } 
                finally {
                    control.setState(TerminalState.CLOSED);
                    
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
        addRascalLinkMouseHandler(vtControl);
        return tm;
    }

    public static void addRascalLinkMouseHandler(VT100TerminalControl control) {
        addMouseHandler(control, new LinkMouseListener());
    }
    

    /**
     * Caveat. We use reflection to get around access restrictions here. An issue
     * has been created to ask for public API to register mouse handlers with the terminal 
     * view.
     */
    private static void addMouseHandler(VT100TerminalControl control, final ITerminalMouseListener listener) {
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
//            RascalTerminalRegistry.getInstance().unregister(this);
        }
    }

    public void setFocus() {
        ((VT100TerminalControl)fControl).setFocus();
//        RascalTerminalRegistry.getInstance().setActive(this);
    }
    
    @Override
    public String getSettingsSummary() {
        return project != null ? "Compiled Rascal Terminal [project: " + project + "]" : "Compiled Rascal Terminal [no project]";
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

}
