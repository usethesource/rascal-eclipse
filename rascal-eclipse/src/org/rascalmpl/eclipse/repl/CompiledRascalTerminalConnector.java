package org.rascalmpl.eclipse.repl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.util.ProjectConfig;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.ideservices.IDEServices;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.repl.CommandExecutor;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.repl.CompiledRascalREPL;
import org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.repl.debug.DebugREPLFrameObserver;
import org.rascalmpl.library.util.PathConfig;
import org.rascalmpl.repl.BaseRascalREPL;
import org.rascalmpl.values.ValueFactoryFactory;

import io.usethesource.vallang.IValueFactory;
import jline.Terminal;
import jline.TerminalFactory;

public class CompiledRascalTerminalConnector extends RascalTerminalConnector {
    
    private final IValueFactory vf = ValueFactoryFactory.getValueFactory();

    @Override
    protected BaseRascalREPL constructRascalREPL(ITerminalControl control, REPLPipedInputStream stdIn, OutputStream stdInUI, Terminal tm) throws IOException, URISyntaxException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject ipr;
        if (root == null || project == null ||  (ipr = root.getProject(project)) == null) {
        	Activator.log("No project selected to configure console for", new NullPointerException());
            return null;
        }
        
        return new CompiledRascalREPL(new ProjectConfig(vf).getPathConfig(ipr), true, true, false, getHistoryFile(), new EclipseIDEServices()) {
            @Override
            protected CommandExecutor constructCommandExecutor(PathConfig pcfg, PrintWriter stdout, PrintWriter stderr, IDEServices ideServices) throws IOException ,org.rascalmpl.library.experiments.Compiler.RVM.Interpreter.NoSuchRascalFunction ,URISyntaxException {
                CommandExecutor exec = new CommandExecutor(pcfg, stdout, stderr, ideServices, null);
                exec.setDebugObserver(new DebugREPLFrameObserver(new ProjectConfig(vf).getPathConfig(ipr), stdIn, stdInUI, true, true, getHistoryFile(), TerminalFactory.get(), new EclipseIDEServices()));                	
                setMeasureCommandTime(true);
                return exec;
            }
            
            @Override
            public void stop() {
                
            }
        };
    }
    
    @Override
    public String getSettingsSummary() {
        return project != null ? "Compiled Rascal Terminal [project: " + project + "]" : "Compiled Rascal Terminal [no project]";
    }
}
