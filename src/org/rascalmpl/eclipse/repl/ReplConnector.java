package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import jline.Terminal;
import jline.TerminalFactory;

import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;
import org.eclipse.tm.terminal.view.ui.streams.InputStreamMonitor;
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
      tm.setEchoEnabled(true);
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
        }
      }.start();
      control.setState(TerminalState.CONNECTED);
    }
    catch (IOException e) {
      e.printStackTrace();
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
