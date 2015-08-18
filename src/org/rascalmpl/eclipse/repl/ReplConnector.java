package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jline.Terminal;
import jline.TerminalFactory;

import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.tm.internal.terminal.emulator.VT100Emulator;
import org.eclipse.tm.internal.terminal.emulator.VT100TerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;
import org.eclipse.tm.internal.terminal.textcanvas.ITextCanvasModel;
import org.eclipse.tm.internal.terminal.textcanvas.TextCanvas;
import org.rascalmpl.interpreter.ConsoleRascalMonitor;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.interpreter.load.StandardLibraryContributor;
import org.rascalmpl.repl.RascalInterpreterREPL;
import org.rascalmpl.values.ValueFactoryFactory;

public class ReplConnector extends TerminalConnectorImpl {

  
  @Override
  public OutputStream getTerminalToRemoteStream() {
    return stdInUI;
  }

  private RascalInterpreterREPL shell;
  private PipedInputStream stdIn;
  private OutputStream stdInUI;
//  private REPLPipedInputStream stdIn;
//  private REPLPipedOutputStream stdInUI;

  @Override
  public boolean isLocalEcho() {
    return false;
  }

  @SuppressWarnings("restriction")
  @Override
  public void connect(ITerminalControl control) {
    super.connect(control);
    try {

//      stdIn = new REPLPipedInputStream();
//      stdInUI = new REPLPipedOutputStream(stdIn);
      stdIn = new PipedInputStream(8*1024);
      stdInUI = new PipedOutputStream(stdIn);
      //stdInUI = new InputStreamMonitor(control, new PipedOutputStream(stdIn), true, "\n"); 

      Terminal tm = TerminalFactory.get();
      tm.setEchoEnabled(false);
      control.setVT100LineWrapping(false);
      VT100Emulator text = ((VT100TerminalControl)control).getTerminalText();
      text.setCrAfterNewLine(true);
       ((VT100TerminalControl)control).setBufferLineLimit(10_000);
       addMouseHandler(((VT100TerminalControl)control), new ITerminalMouseListener() {
        
        @Override
        public void mouseUp(String line, int offset) {
          System.err.println(line + "  offset:" + offset);
        }
        
        @Override
        public void mouseDown(String line, int offset) {
          // TODO Auto-generated method stub
          
        }
        
        @Override
        public void mouseDoubleClick(String line, int offset) {
          // TODO Auto-generated method stub
          
        }
      });
      shell = new RascalInterpreterREPL(stdIn, control.getRemoteToTerminalOutputStream(), true, true, tm) {
        @Override
        protected Evaluator constructEvaluator(Writer stdout, Writer stderr) {
          GlobalEnvironment heap = new GlobalEnvironment();
          ModuleEnvironment root = heap.addModule(new ModuleEnvironment(ModuleEnvironment.SHELL_MODULE, heap));
          IValueFactory vf = ValueFactoryFactory.getValueFactory();
          Evaluator evaluator = new Evaluator(vf, new PrintWriter(stderr), new PrintWriter(stdout), root, heap);
          evaluator.addRascalSearchPathContributor(StandardLibraryContributor.getInstance());
          evaluator.setMonitor(new ConsoleRascalMonitor());
          return evaluator;
        }
      };

      
      new Thread() {
        public void run() {
          try {
            shell.run();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
          finally {
            control.setState(TerminalState.CLOSED);
          }
        }
      }.start();
      control.setState(TerminalState.CONNECTED);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    
  }
  
  
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
            e.printStackTrace();
            throw new RuntimeException(e);
          }
        }

        private String getLine(MouseEvent e) {
          Point pt = screenPointToCell(e.x, e.y);
          return new String(model.getTerminalText().getChars(pt.y));
        }
        private int getOffset(MouseEvent e) {
          Point pt = screenPointToCell(e.x, e.y);
          return pt.x;
        }
        
        @Override
        public void mouseUp(MouseEvent e) {
          listener.mouseUp(getLine(e), getOffset(e));
        }

        @Override
        public void mouseDown(MouseEvent e) {
          listener.mouseDown(getLine(e), getOffset(e));
        }
        
        @Override
        public void mouseDoubleClick(MouseEvent e) {
          listener.mouseDoubleClick(getLine(e), getOffset(e));
        }
      });
    }
    catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    finally {
      
    }
    
  }

  @Override
  protected void doDisconnect() {
    super.doDisconnect();
    if (shell != null) {
      shell.stop();
      shell = null;
    }
  }

  @Override
  public String getSettingsSummary() {
    return "Hooi jong";
  }


}
