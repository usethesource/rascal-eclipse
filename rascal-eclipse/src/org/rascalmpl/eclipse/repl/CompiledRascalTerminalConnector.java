package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.util.ProjectConfig;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.ideservices.BasicIDEServices;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.ideservices.IDEServices;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.repl.CommandExecutor;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.repl.CompiledRascalREPL;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.repl.BaseRascalREPL;
import org.rascalmpl.value.IValueFactory;
import org.rascalmpl.values.ValueFactoryFactory;

import jline.Terminal;

public class CompiledRascalTerminalConnector extends RascalTerminalConnector {
    
    private final IValueFactory vf = ValueFactoryFactory.getValueFactory();

    @Override
    protected BaseRascalREPL constructREPL(ITerminalControl control, REPLPipedInputStream stdIn, OutputStream stdInUI, Terminal tm) throws IOException, URISyntaxException {
        IProject ipr = ResourcesPlugin.getWorkspace().getRoot().getProject(project);

        if (ipr == null) {
            Activator.log("No project selected to configure console for", new NullPointerException());
            return null;
        }
        
        return new CompiledRascalREPL(new ProjectConfig(vf).getPathConfig(ipr), stdIn, control.getRemoteToTerminalOutputStream(), true, true, getHistoryFile(), tm, new BasicIDEServices()) {

            @Override
            protected CommandExecutor constructCommandExecutor(PathConfig pcfg, PrintWriter stdout, PrintWriter stderr, IDEServices ideServices) throws IOException ,org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.NoSuchRascalFunction ,URISyntaxException {
                CommandExecutor exec = new CommandExecutor(pcfg, stdout, stderr, ideServices, null);
                //exec.setDebugObserver(new DebugREPLFrameObserver(reader.getInput(), control.getRemoteToTerminalOutputStream(), true, true, getHistoryFile(), TerminalFactory.get(), new ProjectConfig(vf).getPathConfig(ipr)));
                setMeasureCommandTime(true);
                return exec;
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
    }
    
    @Override
    public String getSettingsSummary() {
        return project != null ? "Compiled Rascal Terminal [project: " + project + "]" : "Compiled Rascal Terminal [no project]";
    }
}
