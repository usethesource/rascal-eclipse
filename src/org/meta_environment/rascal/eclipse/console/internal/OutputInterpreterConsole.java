package org.meta_environment.rascal.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleManager;
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

		commandExecutor = new CommandExecutor(this);
		consoleOutputStream = new ConsoleOutputStream(this);
		
		partitioner = new OutputConsolePartitioner();
		IDocument doc = getDocument();
		doc.setDocumentPartitioner(partitioner);
		partitioner.connect(doc);
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
	
	private void writeToConsole(final String line){
		final IDocument doc = getDocument();

		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				try{
					doc.replace(doc.getLength(), 0, line);
					
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
	
	public void terminate(){
		commandExecutor.terminate();
		interpreter.terminate();
	}
	
	protected void printOutput(String output){
		consoleOutputStream.print();
		writeToConsole(output);
	}
	
	private static class ConsoleOutputStream extends OutputStream{
		private final static int DEFAULT_SIZE = 64;
		
		private byte[] buffer;
		private int index;
		
		private final OutputInterpreterConsole console;
		
		public ConsoleOutputStream(OutputInterpreterConsole console){
			super();
			
			this.console = console;
			
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
			if(index != 0){
				byte[] collectedData = new byte[index + 1];
				System.arraycopy(buffer, 0, collectedData, 0, index);
				collectedData[index] = '\n';
				
				console.writeToConsole(new String(collectedData));
			}
		}
		
		public void reset(){
			buffer = new byte[DEFAULT_SIZE];
			index = 0;
		}
	}
	
	private static class RemoveAction extends Action{
		private final OutputInterpreterConsole console;
		
		public RemoveAction(OutputInterpreterConsole console){
			super("Remove");
			
			this.console = console;
		}
		
		public void run(){
			OutputInterpreterConsole.close(console);
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
		
		private List<String> commandQueue;
		
		private volatile boolean running;
		
		private final NotifiableLock lock = new NotifiableLock();
		
		public CommandExecutor(OutputInterpreterConsole console){
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
				
				if(!running) return;
				
				while(commandQueue.size() > 0){
					console.disableEditing();
					
					String command = commandQueue.remove(0);
					try{
						boolean promptType = console.interpreter.execute(command);
						if(promptType){
							console.printOutput(console.interpreter.getOutput());
						}
					}catch(CommandExecutionException ceex){
						console.printOutput(ceex.getMessage());
					}catch(TerminationException tex){
						// Roll over and die.
						console.terminate();
						return;
					}
				}
			}
		}
		
		public void terminate(){
			running = false;
			lock.wakeUp();
		}
	}
	
	public static OutputInterpreterConsole open(IInterpreter interpreter, String name){
		IConsoleManager fConsoleManager = ConsolePlugin.getDefault().getConsoleManager();
		
		OutputInterpreterConsole console = new OutputInterpreterConsole(interpreter, name);
		fConsoleManager.addConsoles(new IConsole[]{console});
		fConsoleManager.showConsoleView(console);
		
		return console;
	}
	
	public static void close(OutputInterpreterConsole console){
		ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[]{console});
	}
}
