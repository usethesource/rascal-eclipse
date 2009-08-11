package org.meta_environment.rascal.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
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

public class InteractiveInterpreterConsole extends TextConsole implements IInterpreterConsole{
	private final static String CONSOLE_TYPE = InteractiveInterpreterConsole.class.getName();
	
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
		interpreter.setConsole(this); // Sucks but has to happen.
		
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
	}
	
	public void initializeConsole(){
		final Thread commandExecutorThread = new Thread(commandExecutor);
		commandExecutorThread.setDaemon(true);

		// This stinks, but works.
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
	
	public void terminate(){
		commandExecutor.terminate();
		interpreter.terminate();
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
	
	private void writeToConsole(final String line){
		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				try{
					IDocument doc = getDocument();
					doc.replace(doc.getLength(), 0, line);
					
					int endOfDocument = doc.getLength();
					moveCaretTo(endOfDocument);
				}catch(BadLocationException blex){
					// Ignore, never happens.
				}
			}
		});
	}
	
	protected void emitPrompt(){
		writeToConsole(prompt);
	}
	
	protected void emitContinuationPrompt(){
		writeToConsole(continuationPrompt);
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
	
	protected void printOutput(String output){
		consoleOutputStream.print();
		writeToConsole(output);
	}
	
	protected void printOutput(){
		consoleOutputStream.print();
	}
	
	public OutputStream getConsoleOutputStream(){
		return consoleOutputStream;
	}
	
	public void executeCommand(String command){
		final String cmd;
		if(command.endsWith("\n")){
			cmd = command;
		}else{
			StringBuilder sb = new StringBuilder();
			sb.append(command);
			sb.append('\n');
			cmd = sb.toString();
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
	}
	
	private static class ConsoleOutputStream extends OutputStream{
		private final static int DEFAULT_SIZE = 64;
		
		private byte[] buffer;
		private int index;
		
		private final InteractiveInterpreterConsole console;
		
		private volatile boolean enabled;
		
		public ConsoleOutputStream(InteractiveInterpreterConsole console){
			super();
			
			this.console = console;
			
			enabled = false;
			
			reset();
		}
		
		public void write(int arg) throws IOException{
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
		
		public void print(){
			if(index != 0){
				byte[] collectedData = new byte[index + 1];
				System.arraycopy(buffer, 0, collectedData, 0, index);
				collectedData[index] = '\n';
				
				console.writeToConsole(new String(collectedData));
				
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
		
		private StringBuffer buffer;
		
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

		public void documentAboutToBeChanged(DocumentEvent event){
			// Don't care.
		}
		
		public void documentChanged(DocumentEvent event){
			if(!enabled) return;
			
			String text = event.getText();
			
			if(text.equals("\n")){
				if(buffer.length() > 0){ // If we just get a new-line token, execute the current 'thing'.
					buffer.append('\n');
					String command = buffer.toString();
					reset();
					
					console.revertAndAppend(command);
				}else{ // If there is no current 'thing', just execute the '\n' command.
					queue("\n");
					execute();
				}
				return;
			}
			
			int offset = event.getOffset();
			int length = event.getLength();

			int start = offset - console.getInputOffset();
			if(start >= 0){
				buffer.replace(start, start + length, text);
				
				String rest = buffer.toString();
				do{
					int index = rest.indexOf('\n');
					if(index == -1){
						reset();
						buffer.append(rest);
						break;
					}
					
					String command = rest.substring(0, index);
					
					queue(command);
					
					rest = rest.substring(index + 1);
				}while(true);
				
				execute();
			}else{
				console.revertAndAppend(text);
			}
		}
		
		public void queue(String command){
			if(!command.equals("\n")) console.commandHistory.addToHistory(command);
			console.commandExecutor.queue(command);
		}
		
		public void execute(){
			console.commandExecutor.execute();
		}
		
		public void reset(){
			buffer = new StringBuffer();
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
		
		private final List<String> commandQueue;
		
		private volatile boolean running;
		
		private final NotifiableLock lock = new NotifiableLock();
		
		public CommandExecutor(InteractiveInterpreterConsole console){
			super();

			this.console = console;
			
			commandQueue = new ArrayList<String>();
			
			running = false;
		}
		
		public void queue(String command){
			synchronized(commandQueue){
				commandQueue.add(command);
			}
		}
		
		public void execute(){
			lock.wakeUp();
		}
		
		public void run(){
			running = true;
			while(running){
				lock.block();
				
				if(!running) return;
				
				console.disableEditing();
				console.consoleOutputStream.enable();
				
				boolean completeCommand = true;
				while(commandQueue.size() > 0){
					String command = commandQueue.remove(0);
					try{
						completeCommand = console.interpreter.execute(command);
						if(completeCommand){
							console.printOutput(console.interpreter.getOutput());
						}else{
							console.printOutput();
						}
					}catch(CommandExecutionException ceex){
						console.printOutput(ceex.getMessage());
					}catch(TerminationException tex){
						// Roll over and die.
						console.terminate();
						return;
					}
				}
				
				console.consoleOutputStream.disable();
				
				if(completeCommand){
					console.emitPrompt();
				}else{
					console.emitContinuationPrompt();
				}
				
				console.enableEditing();
			}
		}
		
		public void terminate(){
			running = false;
			lock.wakeUp();
		}
	}
}
