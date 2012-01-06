/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Tijs van der Storm - Tijs.van.der.Storm@cwi.nl
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.console;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IProject;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IOConsole;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.ambidexter.ReportView;
import org.rascalmpl.eclipse.console.internal.ConcurrentCircularOutputStream;
import org.rascalmpl.eclipse.console.internal.ConsoleSyncer;
import org.rascalmpl.eclipse.console.internal.IInterpreterConsole;
import org.rascalmpl.eclipse.console.internal.InteractiveInterpreterConsole;
import org.rascalmpl.eclipse.console.internal.OutputInterpreterConsole;
import org.rascalmpl.eclipse.console.internal.StdAndErrorViewPart;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.debug.DebuggableEvaluator;
import org.rascalmpl.interpreter.debug.IDebugger;
import org.rascalmpl.interpreter.env.GlobalEnvironment;
import org.rascalmpl.interpreter.env.ModuleEnvironment;
import org.rascalmpl.values.ValueFactoryFactory;

public class ConsoleFactory{
	public final static String INTERACTIVE_CONSOLE_ID = InteractiveInterpreterConsole.class.getName();
	private final static String SHELL_MODULE = "$shell$";

	private final static IValueFactory vf = ValueFactoryFactory.getValueFactory();
	private final static IConsoleManager fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
	
	private static StdAndErrorViewPart getConsoleViewPart() {
		try {
			return (StdAndErrorViewPart) PlatformUI.getWorkbench()
				    .getActiveWorkbenchWindow()
				    .getActivePage()
					.showView(StdAndErrorViewPart.ID);
		} catch (PartInitException e) {
			Activator.getInstance().writeErrorMsg("Could not get console part");
			return null;
		}
	}
	
	
	private static PrintWriter getErrorWriter() {
		try {
			OutputStream err = getConsoleViewPart().stdError; 
			return new PrintWriter(new OutputStreamWriter(err, "UTF8"),true);
		} catch (UnsupportedEncodingException e) {
			Activator.getInstance().logException("could not get stderr writer", e);
			return new PrintWriter(System.err);
		}
	}
	
	private static PrintWriter getStandardWriter() {
		try {
			OutputStream out = getConsoleViewPart().stdOutput; 
			return new PrintWriter(new OutputStreamWriter(out, "UTF8"));
		} catch (UnsupportedEncodingException e) {
			Activator.getInstance().logException("could not get stdout writer", e);
			return new PrintWriter(System.out);
		}
	}
	
	public ConsoleFactory(){
		super();
	}

	private static class InstanceKeeper{
		private final static ConsoleFactory instance = new ConsoleFactory();
	}

	public static ConsoleFactory getInstance(){
		return InstanceKeeper.instance;
	}
	
	public IRascalConsole openRunConsole(){
		Activator.getInstance().checkRascalRuntimePreconditions();
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new InteractiveRascalConsole(new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openRunConsole(IProject project){
		Activator.getInstance().checkRascalRuntimePreconditions(project);
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new InteractiveRascalConsole(project, new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openRunOutputConsole(){
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new OutputRascalConsole(new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openRunOutputConsole(IProject project){
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new OutputRascalConsole(project, new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}

	public IRascalConsole openDebuggableConsole(IDebugger debugger){
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new InteractiveRascalConsole(debugger, new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openDebuggableConsole(IProject project, IDebugger debugger){
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new InteractiveRascalConsole(project, debugger, new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}

	public IRascalConsole openDebuggableOutputConsole(IDebugger debugger){
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new OutputRascalConsole(debugger, new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public IRascalConsole openDebuggableOutputConsole(IProject project, IDebugger debugger){
		GlobalEnvironment heap = new GlobalEnvironment();
		IRascalConsole console = new OutputRascalConsole(project, debugger, new ModuleEnvironment(SHELL_MODULE, heap), heap);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		return console;
	}
	
	public interface IRascalConsole extends IInterpreterConsole{
		void activate(); // Eclipse thing.
		RascalScriptInterpreter getRascalInterpreter();
		IDocument getDocument();
	    void addHyperlink(IHyperlink hyperlink, int offset, int length) throws BadLocationException;
	}

	private class InteractiveRascalConsole extends InteractiveInterpreterConsole implements IRascalConsole{	
		
		public InteractiveRascalConsole(ModuleEnvironment shell, GlobalEnvironment heap){
			super(new RascalScriptInterpreter(), "Rascal", "rascal>", ">>>>>>>");
			
			getInterpreter().initialize(new Evaluator(vf, getErrorWriter(), getStandardWriter(), shell, heap));
			initializeConsole();
		}
		
		/* 
		 * console associated to a given Eclipse project 
		 * used to initialize the path with modules accessible 
		 * from the selected project and all its referenced projects
		 * */
		public InteractiveRascalConsole(IProject project, ModuleEnvironment shell, GlobalEnvironment heap){
			super(new RascalScriptInterpreter(project), "Rascal ["+project.getName()+"]", "rascal>", ">>>>>>>");
			
			getInterpreter().initialize(new Evaluator(vf, getErrorWriter(), getStandardWriter(), shell, heap));
			initializeConsole();
		}

		public InteractiveRascalConsole(IDebugger debugger, ModuleEnvironment shell, GlobalEnvironment heap){
			super(new RascalScriptInterpreter(), "Rascal", "rascal>", ">>>>>>>");
			
			getInterpreter().initialize(new DebuggableEvaluator(vf, getErrorWriter(), getStandardWriter(),  shell, debugger, heap));
			initializeConsole();
		}
		
		
		public InteractiveRascalConsole(IProject project, IDebugger debugger, ModuleEnvironment shell, GlobalEnvironment heap){
			super(new RascalScriptInterpreter(project), "Rascal ["+project.getName()+"]", "rascal>", ">>>>>>>");
			
			getInterpreter().initialize(new DebuggableEvaluator(vf, getErrorWriter(), getStandardWriter(), shell, debugger, heap));
			initializeConsole();
		}
		
		public RascalScriptInterpreter getRascalInterpreter(){
			return (RascalScriptInterpreter) getInterpreter();
		}
	}

	private class OutputRascalConsole extends OutputInterpreterConsole implements IRascalConsole{
		
		public OutputRascalConsole(ModuleEnvironment shell, GlobalEnvironment heap){
			super(new RascalScriptInterpreter(), "Rascal");
			
			initializeConsole();
			getInterpreter().initialize(new Evaluator(vf, getErrorWriter(), getStandardWriter(), shell, heap));
		}

		public OutputRascalConsole(IDebugger debugger, ModuleEnvironment shell, GlobalEnvironment heap){
			super(new RascalScriptInterpreter(), "Rascal");
			
			initializeConsole();
			getInterpreter().initialize(new DebuggableEvaluator(vf, getErrorWriter(), getStandardWriter(), shell, debugger, heap));
		}

		public OutputRascalConsole(IProject project, IDebugger debugger, ModuleEnvironment shell, GlobalEnvironment heap) {
			super(new RascalScriptInterpreter(project), "Rascal ["+project.getName()+"]");
			
			initializeConsole();
			getInterpreter().initialize(new DebuggableEvaluator(vf, getErrorWriter(), getStandardWriter(), shell, debugger, heap));
		}

		public OutputRascalConsole(IProject project, ModuleEnvironment shell, GlobalEnvironment heap) {
			super(new RascalScriptInterpreter(project), "Rascal ["+project.getName()+"]");
			
			initializeConsole();
			getInterpreter().initialize(new Evaluator(vf, getErrorWriter(), getStandardWriter(), shell, heap));
		}
 
		public RascalScriptInterpreter getRascalInterpreter(){
			return (RascalScriptInterpreter) getInterpreter();
		}
	}
}
