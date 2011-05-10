/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
*******************************************************************************/
package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Vector;

import org.eclipse.imp.preferences.PreferenceConstants;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.part.IPageBookViewPage;

public class InteractiveInterpreterConsole extends TextConsole implements IInterpreterConsole{
	private final static String CONSOLE_TYPE = InteractiveInterpreterConsole.class.getName();
	
	private final static String COMMAND_TERMINATOR = System.getProperty("line.separator");
	
	private final IInterpreter interpreter;
	
	private final CommandExecutor commandExecutor;
	private final CommandHistory commandHistory;
	private final ConsoleDocumentListener documentListener;
	private final ConsoleOutputStream consoleOutputStream;
	
	private final InterpreterConsolePartitioner partitioner;
	private volatile TextConsolePage page;
	
	private final String prompt;
	private final String continuationPrompt;
	
	private int inputOffset;
	private String currentContent;
	
	public InteractiveInterpreterConsole(IInterpreter interpreter, String name, String prompt, String continuationPrompt){
		super(name, CONSOLE_TYPE, null, false);
		
		this.interpreter = interpreter;
		commandExecutor = new CommandExecutor(this);
		consoleOutputStream = new ConsoleOutputStream(this);
		interpreter.setConsole(this); 
		
		this.prompt = prompt;
		this.continuationPrompt = continuationPrompt;
		
		commandHistory = new CommandHistory();
		documentListener = new ConsoleDocumentListener(this);
		documentListener.registerListener();
		
		partitioner = new InterpreterConsolePartitioner();
		IDocument doc = getDocument();
		doc.setDocumentPartitioner(partitioner);
		partitioner.connect(doc);
		
		documentListener.enable();
		
		setFont();
	}
	
	private void setFont(){
		final FontRegistry fontRegistry= RuntimePlugin.getInstance().getFontRegistry();
		final String fontDescriptor = RuntimePlugin.getInstance().getPreferenceStore().getString(PreferenceConstants.P_SOURCE_FONT);
		
		if (fontDescriptor != null) {
			if (!fontRegistry.hasValueFor(fontDescriptor)) {
				FontData[] fontData= PreferenceConverter.readFontData(fontDescriptor);
				fontRegistry.put(fontDescriptor, fontData);
			}
			
			Display.getDefault().syncExec(new Runnable(){
				public void run(){
					Font sourceFont= fontRegistry.get(fontDescriptor);
					setFont(sourceFont);
				}
			});
		}
	}
	
	public void initializeConsole(){
		final Thread commandExecutorThread = new Thread(commandExecutor);
		commandExecutorThread.setDaemon(true);
		commandExecutorThread.setName("Console Command Executor");

		// TODO: refactor to use an Eclipse provided extension point for console toolbar items
		new Thread(){
			public void run(){
				do{
					Thread.yield();
				}while(page == null);
				
				disableEditing();
				emitPrompt();
				enableEditing();
				
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
						toolBarManager.add(new StoreHistoryAction(InteractiveInterpreterConsole.this));
						toolBarManager.add(new TerminationAction(InteractiveInterpreterConsole.this));
						toolBarManager.add(new InterruptAction(InteractiveInterpreterConsole.this));
						toolBarManager.add(new TraceAction(InteractiveInterpreterConsole.this));
						
						actionBars.updateActionBars();
						
						commandExecutorThread.start();

					}
				});
			}
		}.start();
	}
	
	private static class StoreHistoryAction extends Action{
		private final InteractiveInterpreterConsole console;
		
		public StoreHistoryAction(InteractiveInterpreterConsole console){
			super("Store history");
			
			this.console = console;
		}
		
		public void run(){
			console.interpreter.storeHistory(console.commandHistory);
		}
	}
	
	private static class TerminationAction extends Action{
		private final InteractiveInterpreterConsole console;
		
		public TerminationAction(InteractiveInterpreterConsole console){
			super("Terminate");
			
			this.console = console;
		}
		
		public void run(){
			console.terminate();
		}
	}
	
	private static class TraceAction extends Action{
		private final InteractiveInterpreterConsole console;
		
		public TraceAction(InteractiveInterpreterConsole console){
			super("Trace");
			
			this.console = console;
		}
		
		public void run(){
			console.printTrace();
		}
	}
	
	private static class InterruptAction extends Action{
		private final InteractiveInterpreterConsole console;
		
		public InterruptAction(InteractiveInterpreterConsole console){
			super("Interrupt");
			
			this.console = console;
		}
		
		public void run(){
			console.interrupt();
		}
	}
	
	public void terminate(){
		partitionerFinished();
		documentListener.deregisterListener();
		
		commandExecutor.terminate();
		interpreter.interrupt();
		interpreter.terminate();
	}
	
	public void printTrace() {
		String trace = interpreter.getTrace();
		writeToConsole(trace, true);
	}

	public void interrupt() {
		interpreter.interrupt();
	}
	
	public IInterpreter getInterpreter(){
		return interpreter;
	}
	
	public boolean hasHistory(){
		return true;
	}
	
	public CommandHistory getHistory(){
		return commandHistory;
	}
	
	protected IConsoleDocumentPartitioner getPartitioner(){
		return partitioner;
	}

	public IPageBookViewPage createPage(IConsoleView view){
		return (page = new InterpreterConsolePage(this, view));
	}
	
	private void writeToConsole(final String line, final boolean terminateLine){
		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				try{
					IDocument doc = getDocument();
					doc.replace(doc.getLength(), 0, line);
					if(terminateLine) doc.replace(doc.getLength(), 0, COMMAND_TERMINATOR);
					
					int endOfDocument = doc.getLength();
					moveCaretTo(endOfDocument);
				}catch(BadLocationException blex){
					// Ignore, never happens.
				}catch(NullPointerException npex){
					// Ignore, happens when the evaluator is interrupted before terminating.
				}
			}
		});
	}
	
	protected void emitPrompt(){
		writeToConsole(prompt, false);
	}
	
	protected void emitContinuationPrompt(){
		writeToConsole(continuationPrompt, false);
	}
	
	protected void enableEditing(){
		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				documentListener.enable();
				page.getViewer().setEditable(true);
				
				IDocument doc = getDocument();
				inputOffset = doc.getLength(); // Update the input offset; note however that this is not the proper place to do this (just the most convenient).
				currentContent = doc.get();
			}
		});
	}
	
	protected void disableEditing(){
		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				page.getViewer().setEditable(false);
				documentListener.disable();
			}
		});
	}
	
	protected String getCurrentConsoleInput(){
		return documentListener.getCurrentBufferContent();
	}
	
	protected void printOutput(String output){
		consoleOutputStream.print();
		writeToConsole(output, true);
	}
	
	protected void printOutput(){
		consoleOutputStream.print();
	}
	
	protected void setError(final int offset, final int length){
		Display.getDefault().asyncExec(new Runnable(){
			public void run(){
				TextConsoleViewer consoleViewer = page.getViewer();
				StyledText styledText = consoleViewer.getTextWidget();
				Display currentDisplay = Display.getCurrent();
				if((offset > 0) && (offset < getDocument().getLength())) styledText.setStyleRange(new StyleRange(offset, (length != 0) ? length : 1, new Color(currentDisplay, 255, 0, 0), new Color(currentDisplay, 255, 255, 255), SWT.NORMAL));
			}
		});
	}
	
	public OutputStream getConsoleOutputStream(){
		return consoleOutputStream;
	}
	
	public void executeCommand(String command){
		final String cmd;
		if(command.endsWith(COMMAND_TERMINATOR)){
			cmd = command;
		}else{
			cmd = command.concat(COMMAND_TERMINATOR);
		}
		
		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				IDocument doc = getDocument();
				try{
					doc.replace(inputOffset, doc.getLength() - inputOffset, cmd);
				}catch(BadLocationException blex){
					// Ignore, can't happen.
				}
				
				moveCaretTo(doc.getLength());
			}
		});
	}
	
	public int getInputOffset(){
		return inputOffset;
	}
	
	public void revertAndAppend(final String input){
		Display.getDefault().asyncExec(new Runnable(){
			public void run(){
				page.getViewer().setEditable(false);
				documentListener.disable();
				
				// Reset the content.
				IDocument doc = getDocument();
				doc.set(currentContent);
				
				documentListener.enable();
				
				try{
					doc.replace(doc.getLength(), 0, input);
				}catch(BadLocationException blex){
					// Ignore, can't happen.
				}
				
				// Move the cursor to the end.
				moveCaretTo(doc.getLength());
				
				page.getViewer().setEditable(true);
			}
		});
	}
	
	public void historyCommand(final String command){
		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				IDocument doc = getDocument();
				try{
					doc.replace(inputOffset, doc.getLength() - inputOffset, command);
				}catch(BadLocationException blex){
					// Ignore, can't happen.
				}
				
				moveCaretTo(doc.getLength());
			}
		});
	}
	
	// Only call from inside the UI-thread.
	private void moveCaretTo(int index){
		TextConsoleViewer consoleViewer = page.getViewer();
		StyledText styledText = consoleViewer.getTextWidget();
		
		styledText.setCaretOffset(index);
		consoleViewer.revealRange(index, 0);
	}
	
	private static class InterpreterConsolePage extends TextConsolePage{
		private final InteractiveInterpreterConsole console;
		
		public InterpreterConsolePage(InteractiveInterpreterConsole console, IConsoleView view){
			super(console, view);
			
			this.console = console;
		}

		public TextConsoleViewer createViewer(Composite parent){
			return new InterpreterConsoleViewer(console, parent);
		}
		
		public void createActions(){
			// Don't do anything.
		}
		
		public void contextMenuAboutToShow(IMenuManager menuManager){
			// Don't do anything.
		}
		
		public void configureToolBar(IToolBarManager mgr){
			// Don't do anything.
		}
	}
	
	protected static class ConsoleOutputStream extends OutputStream{
		private final static int DEFAULT_SIZE = 64;
		
		private byte[] buffer;
		private int index;
		
		private final InteractiveInterpreterConsole console;
		
		private boolean enabled;

		private final PrintStream backup;
		
		private ConsoleOutputStream(InteractiveInterpreterConsole console){
			super();
			
			this.console = console;
			
			enabled = false;
			
			this.backup = RuntimePlugin.getInstance().getConsoleStream();
			
			reset();
		}
		
		public synchronized void write(byte[] bytes, int offset, int length) throws IOException {
			if(!enabled){
				backup.write(bytes,offset,length);
				return;
			}
			
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
			if(!enabled){
				backup.write(arg);
				return;
			}
			
			if(arg == '\n' || arg == '\r'){ // If we encounter a new-line, print the content of the buffer.
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
		
		public synchronized void enable(){
			enabled = true;
		}
		
		public synchronized void disable(){
			enabled = false;
		}
		
		private void reset(){
			buffer = new byte[DEFAULT_SIZE];
			index = 0;
		}
	}
	
	private static class InterpreterConsolePartitioner extends FastPartitioner implements IConsoleDocumentPartitioner{
		
		public InterpreterConsolePartitioner(){
			super(new RuleBasedPartitionScanner(), new String[]{});
		}

		public StyleRange[] getStyleRanges(int offset, int length){
			return new StyleRange[]{new StyleRange(offset, length, null, null, SWT.NORMAL)};
		}

		public boolean isReadOnly(int offset){
			return false;
		}
	}
	
	private static class ConsoleDocumentListener implements IDocumentListener{
		private final InteractiveInterpreterConsole console;
		
		private volatile boolean enabled;
		
		private final StringBuffer buffer;
		
		public ConsoleDocumentListener(InteractiveInterpreterConsole console){
			super();
			
			this.console = console;
			
			buffer = new StringBuffer();
			
			enabled = false;
		}
		
		public void enable(){
			enabled = true;
		}
		
		public void disable(){
			enabled = false;
		}
		
		public void registerListener(){
			IDocument doc = console.getDocument();
			doc.addDocumentListener(this);
		}
		
		public void deregisterListener(){
			IDocument doc = console.getDocument();
			doc.removeDocumentListener(this);
		}

		public void documentAboutToBeChanged(DocumentEvent event){
			// Don't care.
		}
		
		public void documentChanged(DocumentEvent event){
			if(!enabled) return;
			
			String text = event.getText();
			
			if(text.equals(COMMAND_TERMINATOR)){
				if(buffer.length() > 0){ // If we just get a new-line token, execute the current 'thing'.
					buffer.append(COMMAND_TERMINATOR);
					String command = buffer.toString();
					reset();
					
					console.revertAndAppend(command);
				}else{ // If there is no current 'thing', just execute the command terminator ('\n', '\r' or '\r\n') command.
					queue(COMMAND_TERMINATOR, console.getInputOffset());
					execute();
				}
				return;
			}
			
			int offset = event.getOffset();
			int length = event.getLength();

			int start = offset - console.getInputOffset();
			if(start >= 0){
				buffer.replace(start, start + length, text);
				
				int commandStartOffset = console.getInputOffset();
				
				String rest = buffer.toString();
				boolean commandsQueued = false;
				do{
					int index = rest.indexOf(COMMAND_TERMINATOR);
					if(index == -1){
						reset();
						buffer.append(rest);
						break;
					}
					
					String command = rest.substring(0, index);
					
					queue(command, commandStartOffset);
					commandStartOffset += index;
					commandsQueued = true;
					
					rest = rest.substring(index + COMMAND_TERMINATOR.length());
				}while(true);
				
				if(commandsQueued) execute();
			}else{
				buffer.insert(0, text);
				String toAppend = buffer.toString();
				reset();
				console.revertAndAppend(toAppend);
			}
		}
		
		public String getCurrentBufferContent(){
			return buffer.toString();
		}
		
		public void queue(String command, int commandStartOffset){
			if(!command.equals(COMMAND_TERMINATOR)) console.commandHistory.addToHistory(command);
			console.commandExecutor.queue(command, commandStartOffset);
		}
		
		public void execute(){
			console.commandExecutor.execute();
		}
		
		public void reset(){
			buffer.delete(0, buffer.length());
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
		private final InteractiveInterpreterConsole console;
		
		private final Vector<Command> commandQueue;
		
		private volatile boolean running;
		
		private final NotifiableLock lock = new NotifiableLock();
		
		public CommandExecutor(InteractiveInterpreterConsole console){
			super();

			this.console = console;
			
			commandQueue = new Vector<Command>();
			
			running = false;
		}
		
		private static class Command{
			public final String command;
			public final int commandStartOffset;
			
			public Command(String command, int commandStartOffset){
				super();
				
				this.command = command;
				this.commandStartOffset = commandStartOffset;
			}
		}
		
		public void queue(String command, int commandStartOffset){
			commandQueue.add(new Command(command, commandStartOffset));
		}
		
		public void execute(){
			lock.wakeUp();
		}
		
		public void run(){
			running = true;
			while(running){
				lock.block();
				
				if(!running) return;
				
				if(commandQueue.size() > 0){
					console.disableEditing();
					console.consoleOutputStream.enable();
					
					boolean completeCommand = true;
					int completeCommandStartOffset = -1;
					
					do{
						Command command = commandQueue.remove(0);
						if(completeCommandStartOffset == -1) completeCommandStartOffset = command.commandStartOffset;
						try{
							completeCommand = console.interpreter.execute(command.command);
							
							if(completeCommand){
								console.printOutput(console.interpreter.getOutput());
								completeCommandStartOffset = -1; // Reset offset.
							}else{
								console.printOutput();
							}
						}catch(CommandExecutionException ceex){
							console.printOutput(ceex.getMessage());
							int errorOffset = ceex.getOffset();
							int errorLength = ceex.getLength();
							if(errorOffset != -1 && errorLength != -1) console.setError(completeCommandStartOffset + errorOffset, errorLength);
							completeCommand = true;
						}catch(TerminationException tex){
							// Roll over and die.
							console.terminate();
							return;
						}
					}while(commandQueue.size() > 0);
					
					if(!running) return;
					
					console.consoleOutputStream.disable();
					
					if(completeCommand){
						console.emitPrompt();
					}else{
						console.emitContinuationPrompt();
					}
					
					console.enableEditing();
				}
			}
		}
		
		public void terminate(){
			running = false;
			lock.wakeUp();
		}
	}
}
