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
package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.part.IPageBookViewPage;

public class OutputInterpreterConsole extends TextConsole implements IInterpreterConsole{
	private final static String CONSOLE_TYPE = OutputInterpreterConsole.class.getName();
	
	private final IInterpreter interpreter;

	private final CommandExecutor commandExecutor;
	private final OutputConsolePartitioner partitioner;
	private final ConsoleOutputStream consoleOutputStream;
	
	private volatile TextConsolePage page;
	
	public OutputInterpreterConsole(IInterpreter interpreter, String name){
		super(name, CONSOLE_TYPE, null, false);
		
		this.interpreter = interpreter;
		interpreter.setConsole(this);

		commandExecutor = new CommandExecutor(this);
		consoleOutputStream = new ConsoleOutputStream(this);
		
		partitioner = new OutputConsolePartitioner();
		IDocument doc = getDocument();
		doc.setDocumentPartitioner(partitioner);
		partitioner.connect(doc);
	}
	
	public boolean hasHistory(){
		return false;
	}
	
	public CommandHistory getHistory(){
		throw new UnsupportedOperationException("History commands are not supported by non-interactive consoles.");
	}
	
	public void initializeConsole(){
		final Thread commandExecutorThread = new Thread(commandExecutor);
		commandExecutorThread.setDaemon(true);
		commandExecutorThread.setName("Console Command Executor");
		
		// This stinks, but works.
		new Thread(){
			public void run(){
				do{
					Thread.yield();
				}while(page == null);
				
				disableEditing();
				
				Display.getDefault().asyncExec(new Runnable(){
					public void run(){
						IActionBars actionBars = page.getSite().getActionBars();
						IToolBarManager toolBarManager = actionBars.getToolBarManager();
						
						// Removed default stuff.
						IContributionItem[] ci = toolBarManager.getItems();
						for(int i = 0; i < ci.length; i++){
							toolBarManager.remove(ci[i]);
						}
						
						// Add custom stuff.
						toolBarManager.add(new RemoveAction(OutputInterpreterConsole.this));
						toolBarManager.add(new InterruptAction(OutputInterpreterConsole.this));
						
						actionBars.updateActionBars();
						
						commandExecutorThread.start();
					}
				});
			}
		}.start();
	}
	
	private void disableEditing(){
		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				page.getViewer().setEditable(false);
			}
		});
	}
	
	private void writeToConsole(final String line, final boolean terminateLine){
		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				IDocument doc = getDocument();
				try{
					doc.replace(doc.getLength(), 0, line);
					if(terminateLine) doc.replace(doc.getLength(), 0, "\n");
					
					int endOfDocument = doc.getLength();
					moveCaretTo(endOfDocument);
				}catch(BadLocationException blex){
					// Ignore, never happens.
				}
			}
		});
	}
	
	private void moveCaretTo(int index){
		TextConsoleViewer consoleViewer = page.getViewer();
		StyledText styledText = consoleViewer.getTextWidget();
		
		styledText.setCaretOffset(index);
		consoleViewer.revealRange(index, 0);
	}
	
	public OutputStream getConsoleOutputStream(){
		return consoleOutputStream;
	}
	
	public IInterpreter getInterpreter(){
		return interpreter;
	}
	
	public void executeCommand(String command){
		commandExecutor.execute(command);
	}

        /**
         * Do not call this method unless you're absolutely sure you
         * understand the concurrency implications. (Arnold Lankamp)
         */	 
	public void executeCommandAndWait(String command){
		commandExecutor.executeAndWait(command);
	}

        /**
         * Do not call this method unless you're absolutely sure you
         * understand the concurrency implications. (Arnold Lankamp)
         */	 
	public void unblock(){
		commandExecutor.unblock();
	}
	
	public void interrupt() {
		interpreter.interrupt();
	}
	
	private static class InterruptAction extends Action{
		private final OutputInterpreterConsole console;
		
		public InterruptAction(OutputInterpreterConsole console){
			super("Interrupt");
			
			this.console = console;
		}
		
		public void run(){
			console.interrupt();
		}
	}
	
	public void terminate(){
		partitionerFinished();
		
		commandExecutor.terminate();
		interpreter.interrupt();
		interpreter.terminate();
	}
	
	protected void printOutput(String output){
		consoleOutputStream.print();
		writeToConsole(output, true);
	}
	
	private static class ConsoleOutputStream extends OutputStream{
		private final static int DEFAULT_SIZE = 64;
		
		private byte[] buffer;
		private int index;
		
		private final OutputInterpreterConsole console;
		
		private volatile boolean enabled;
		
		public ConsoleOutputStream(OutputInterpreterConsole console){
			super();
			
			this.console = console;
			
			enabled = false;
			
			reset();
		}
		
		public synchronized void write(byte[] bytes, int offset, int length) throws IOException {
			if(!enabled) throw new RuntimeException("Unable to write data while no commands are being executed");
			
			int currentSize = buffer.length;
			if(index + length >= currentSize){
				int newSize = currentSize;
				do{
					newSize <<= 1;
				}while(newSize < (index + length));
				
				byte[] newBuffer = new byte[newSize << 1];
				System.arraycopy(buffer, 0, newBuffer, 0, currentSize);
				buffer = newBuffer;
			}
			
			System.arraycopy(bytes, offset, buffer, index, length);
			index += length;
			print();
		}
		
		public synchronized void write(int arg) throws IOException{
			if(!enabled) throw new RuntimeException("Unable to write data while no commands are being executed");
			
			if(arg == '\n'){ // If we encounter a new-line, print the content of the buffer.
				print();
				return;
			}
			
			int currentSize = buffer.length;
			if(index == currentSize){
				byte[] newData = new byte[currentSize << 1];
				System.arraycopy(buffer, 0, newData, 0, currentSize);
				buffer = newData;
			}
			
			buffer[index++] = (byte) arg;
		}
		
		public synchronized void print(){
			if(index != 0){
				byte[] collectedData = new byte[index];
				System.arraycopy(buffer, 0, collectedData, 0, index);
				
				console.writeToConsole(new String(collectedData), false);
				
				reset();
			}
		}
		
		public void enable(){
			enabled = true;
		}
		
		public void disable(){
			enabled = false;
		}
		
		public void reset(){
			buffer = new byte[DEFAULT_SIZE];
			index = 0;
		}
	}
	
	private static class RemoveAction extends Action{
		private final OutputInterpreterConsole console;
		
		public RemoveAction(OutputInterpreterConsole console){
			super("Close");
			
			this.console = console;
		}
		
		public void run(){
			console.terminate();
		}
	}
	
	protected IConsoleDocumentPartitioner getPartitioner(){
		return partitioner;
	}
	
	private static class OutputConsolePartitioner extends FastPartitioner implements IConsoleDocumentPartitioner{
		
		public OutputConsolePartitioner(){
			super(new RuleBasedPartitionScanner(), new String[]{});
		}

		public StyleRange[] getStyleRanges(int offset, int length){
			return new StyleRange[]{new StyleRange(offset, length, null, null, SWT.NORMAL)};
		}

		public boolean isReadOnly(int offset){
			return false;
		}
	}

	public IPageBookViewPage createPage(IConsoleView view){
		return (page = new OutputConsolePage(this, view));
	}
	
	private static class OutputConsolePage extends TextConsolePage{
		private final OutputInterpreterConsole console;
		
		public OutputConsolePage(OutputInterpreterConsole console, IConsoleView view){
			super(console, view);
			
			this.console = console;
		}
		
		public TextConsoleViewer createViewer(Composite parent){
			return new TextConsoleViewer(parent, console);
		}
	}
	
	private static class NotifiableLock{
		private boolean notified = false;
		
		public synchronized void block(){
			while(!notified){
				try{
					wait();
				}catch(InterruptedException irex){
					// Ignore.
				}
			}
			notified = false;
		}
		
		public synchronized void wakeUp(){
			notified = true;
			notify();
		}
	}
	
	private static class CommandExecutor implements Runnable{
		private final OutputInterpreterConsole console;
		
		private final Vector<String> commandQueue;
		
		private volatile boolean running;
		
		private final NotifiableLock lock = new NotifiableLock();
		
		private final NotifiableLock waitLock = new NotifiableLock();
		
		public CommandExecutor(OutputInterpreterConsole console){
			super();

			this.console = console;
			
			commandQueue = new Vector<String>();
			
			running = false;
		}
		
		public void execute(String command){
			commandQueue.add(command);
			lock.wakeUp();
		}
		
		public void executeAndWait(String command){
			commandQueue.add(command);
			lock.wakeUp();
			waitLock.block();
		}
		
		public void unblock(){
			waitLock.wakeUp();
		}
		
		public void run(){
			running = true;
			while(running){
				lock.block();
				
				if(!running) return;
				
				console.consoleOutputStream.enable();
				
				while(commandQueue.size() > 0){
					String command = commandQueue.remove(0);
					try{
						boolean completeCommand = console.interpreter.execute(command);
						if(completeCommand){
							console.printOutput(console.interpreter.getOutput());
						}
					}catch(CommandExecutionException ceex){
						console.printOutput(ceex.getMessage());
					}catch(TerminationException tex){
						// Roll over and die.
						console.terminate();
						return;
					}finally{
						unblock();
					}
				}
				
				console.consoleOutputStream.disable();
			}
		}
		
		public void terminate(){
			running = false;
			lock.wakeUp();
		}
	}
}
