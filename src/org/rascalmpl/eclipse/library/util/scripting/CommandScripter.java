/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.library.util.scripting;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.imp.pdb.facts.IBool;
import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.rascalmpl.eclipse.console.ConsoleFactory;
import org.rascalmpl.eclipse.console.internal.OutputInterpreterConsole;

public class CommandScripter{
	private static final IWorkspaceRoot WORKSPACE = ResourcesPlugin.getWorkspace().getRoot();
	
	public CommandScripter(IValueFactory valueFactory){
		super();
	}
	
	public void execute(IList commandsList, IList projectsList, IBool closeConsolesValue, IInteger timeoutValue){
		String[] commands = new String[commandsList.length()];
		for(int i = commandsList.length() - 1; i >= 0; i--){
			commands[i] = ((IString) commandsList.get(i)).getValue();
		}
		
		IProject[] projects = new IProject[projectsList.length()];
		for(int i = projectsList.length() - 1; i >= 0; i--){
			projects[i] = WORKSPACE.getProject(((ISourceLocation) projectsList.get(i)).getURI().getHost());
		}
		
		boolean closeConsoles = closeConsolesValue.getValue();
		int timeout = timeoutValue.intValue();
		
		ConsoleFactory cf = ConsoleFactory.getInstance();
		
		for(int i = 0 ; i < projects.length; i++){
			IProject project = projects[i];
			DummyDebugger dd = new DummyDebugger();
			OutputInterpreterConsole console = (OutputInterpreterConsole) cf.openDebuggableOutputConsole(project, dd);
			TheTerminator t = TheTerminator.killedInTMinus(timeout, console, dd);
			try{
				for(int j = 0; j < commands.length; j++){
					console.executeCommandAndWait(commands[j]+"\n");
				}
			}catch(RuntimeException rex){
				System.err.println("Failed to execute commands in project: "+project+", cause: "+rex.getMessage());
			}
			
			t.illBeBack();
			console.unblock();
			if(closeConsoles) console.terminate();
		}
	}
	
	private static class TheTerminator implements Runnable{
		private final OutputInterpreterConsole console;
		private final DummyDebugger dd;
		private final int timeout;
		
		private final NotifiableLock lock;
		private volatile boolean stopped;
		
		public TheTerminator(OutputInterpreterConsole console, DummyDebugger dd, int timeout){
			super();
			
			this.console = console;
			this.dd = dd;
			this.timeout = timeout;
			
			lock = new NotifiableLock();
			stopped = false;
		}
		
		public void run(){
			lock.block(timeout);
			
			if(!stopped){
				System.err.println("Killing!");
				dd.destroy();
				console.terminate();
			}
		}
		
		public void illBeBack(){
			stopped = true;
			lock.wakeUp();
		}
		
		private static class NotifiableLock{
			private volatile boolean notified = false;
			
			public synchronized void wakeUp(){
				notified = true;
				notify();
			}
			
			public synchronized void block(int timeout){
				long endTime = System.currentTimeMillis() + timeout;
				while(!notified && endTime > System.currentTimeMillis()){
					long waitFor = endTime - System.currentTimeMillis();
					try{
						wait(waitFor);
					}catch(InterruptedException irex){
						// Don't care.
					}
				}
				notified = false;
			}
		}
		
		public static TheTerminator killedInTMinus(int xSeconds, OutputInterpreterConsole console, DummyDebugger dd){
			TheTerminator t = new TheTerminator(console, dd, xSeconds * 1000);
			new Thread(t).start();
			return t;
		}
	}
}
