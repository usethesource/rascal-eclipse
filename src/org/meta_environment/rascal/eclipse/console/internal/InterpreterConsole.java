package org.meta_environment.rascal.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.part.IPageBookViewPage;

public class InterpreterConsole extends TextConsole{
	private final static String CONSOLE_TYPE = InterpreterConsole.class.getName();
	
	private final IInterpreter interpreter;
	
	private final CommandExecutor commandExecutor;
	private final CommandHistory commandHistory;
	private final ConsoleDocumentListener documentListener;
	private final ConsoleOutputStream consoleOutputStream;
	
	private final InterpreterConsolePartitioner partitioner;
	private TextConsolePage page;
	
	private final String prompt;
	private final String continuationPrompt;
	
	public InterpreterConsole(IInterpreter interpreter, String name, String prompt, String continuationPrompt){
		super(name, CONSOLE_TYPE, null, false);
		
		this.interpreter = interpreter;
		commandExecutor = new CommandExecutor(this);
		consoleOutputStream = new ConsoleOutputStream(this);
		interpreter.setConsole(this); // Sucks but has to happen.
		
		this.prompt = prompt;
		this.continuationPrompt = continuationPrompt;
		
		commandHistory = new CommandHistory();
		documentListener = new ConsoleDocumentListener(this);
		documentListener.registerListener();
		
		partitioner = new InterpreterConsolePartitioner();
		getDocument().setDocumentPartitioner(partitioner);
		partitioner.connect(getDocument());
		
		documentListener.enable();
	}
	
	public void initializeConsole(){
		Thread commandExecutorThread = new Thread(commandExecutor);
		commandExecutorThread.setDaemon(true);
		commandExecutorThread.start();

		// This stinks, but works.
		Runnable r = new Runnable(){
			public void run(){
				if(page == null) Display.getDefault().asyncExec(this);
				
				disableEditing();
				emitPrompt();
				enableEditing();
			}
		};
		Display.getDefault().asyncExec(r);
	}
	
	public void terminate(){
		commandExecutor.terminate();
	}
	
	public IInterpreter getInterpreter(){
		return interpreter;
	}
	
	protected IConsoleDocumentPartitioner getPartitioner(){
		return partitioner;
	}

	public IPageBookViewPage createPage(IConsoleView view){
		return (page = new TextConsolePage(this, view));
	}
	
	private void writeToConsole(final String line){
		final IDocument doc = getDocument();

		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				try{
					doc.replace(doc.getLength(), 0, line);
					
					int moveToOffset = doc.getLength();
					page.getViewer().getTextWidget().setCaretOffset(moveToOffset);
					page.getViewer().revealRange(moveToOffset, 0);
				}catch(BadLocationException blex){
					// Ignore, never happens.
				}
			}
		});
	}
	
	private void emitPrompt(){
		writeToConsole(prompt);
	}
	
	private void emitContinuationPrompt(){
		writeToConsole(continuationPrompt);
	}
	
	protected void enableEditing(){
		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				documentListener.enable();
				page.getViewer().setEditable(true);
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
	
	protected void printOutput(String output){
		consoleOutputStream.print();
		writeToConsole(output);
		emitPrompt();
	}
	
	protected void printContinuationPrompt(){
		consoleOutputStream.print();
		emitContinuationPrompt();
	}
	
	public OutputStream getConsoleOutputStream(){
		return new ConsoleOutputStream(this);
	}
	
	public void executeCommand(String command){
		commandExecutor.execute(command);
	}
	
	private static class ConsoleOutputStream extends OutputStream{
		private final static int DEFAULT_SIZE = 64;
		
		private byte[] buffer;
		private int index;
		
		private final InterpreterConsole rascalConsole;
		
		public ConsoleOutputStream(InterpreterConsole rascalConsole){
			super();
			
			this.rascalConsole = rascalConsole;
			
			reset();
		}
		
		public void write(int arg) throws IOException{
			if(arg == '\n'){ // If we encounter a new-line, print the content of the buffer.
				print();
				reset();
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
		
		public void print(){
			byte[] collectedData = new byte[index];
			System.arraycopy(buffer, 0, collectedData, 0, index);
			
			rascalConsole.writeToConsole(new String(collectedData));
		}
		
		public void reset(){
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
		private final InterpreterConsole console;
		
		private volatile boolean enabled;
		
		private StringBuffer buffer;
		
		public ConsoleDocumentListener(InterpreterConsole console){
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

		public void documentAboutToBeChanged(DocumentEvent event){
			// Don't care.
		}
		
		public void documentChanged(DocumentEvent event){ // TODO Fix text editing stuff.
			if(!enabled) return;
			
			String text = event.getText();
			buffer.append(text);
			
			String rest = buffer.toString();
			do{
				int index = rest.indexOf('\n');
				if(index == -1){
					buffer = new StringBuffer();
					buffer.append(rest);
					break;
				}
				
				String command = rest.substring(0, index);
				
				console.commandHistory.addToHistory(command);
				console.commandExecutor.execute(command);
				
				rest = rest.substring(index + 1); // Does this work?
			}while(true);
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
		private final InterpreterConsole console;
		
		private List<String> commandQueue;
		
		private volatile boolean running;
		
		private final NotifiableLock lock = new NotifiableLock();
		
		public CommandExecutor(InterpreterConsole console){
			super();

			this.console = console;
			
			commandQueue = new ArrayList<String>();
			
			running = false;
		}
		
		public void execute(String command){
			synchronized(commandQueue){
				commandQueue.add(command);
				lock.wakeUp();
			}
		}
		
		public void run(){
			running = true;
			while(running){
				lock.block();
				
				while(commandQueue.size() > 0){
					console.disableEditing();
					
					String command = commandQueue.remove(0);
					try{
						boolean promptType = console.interpreter.execute(command);
						if(promptType){
							console.printOutput(console.interpreter.getOutput());
						}else{
							console.printContinuationPrompt();
						}
					}catch(CommandExecutionException ceex){
						console.printOutput(ceex.getMessage());
					}
				}
				
				console.enableEditing();
			}
		}
		
		public void terminate(){
			running = false;
		}
	}
}
