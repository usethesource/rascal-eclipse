package org.meta_environment.rascal.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;

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
	
	private final CommandHistory commandHistory;
	private final ConsoleDocumentListener documentListener;
	private final ConsoleOutputStream consoleOutputStream;
	
	private final InterpreterConsolePartitioner partitioner;
	private InterpreterConsolePage page;
	
	private final String prompt;
	private final String continuationPrompt;
	
	public InterpreterConsole(IInterpreter interpreter, String name, String prompt, String continuationPrompt){
		super(name, CONSOLE_TYPE, null, false);
		
		this.interpreter = interpreter;
		consoleOutputStream = new ConsoleOutputStream(this);
		interpreter.setOutputStream(consoleOutputStream);
		
		this.prompt = prompt;
		this.continuationPrompt = continuationPrompt;
		
		commandHistory = new CommandHistory();
		documentListener = new ConsoleDocumentListener(this);
		
		partitioner = new InterpreterConsolePartitioner();
		
		emitPrompt();
	}
	
	protected IConsoleDocumentPartitioner getPartitioner(){
		return partitioner;
	}

	public IPageBookViewPage createPage(IConsoleView view){
		return (page = new InterpreterConsolePage(this, view));
	}
	
	private void writeToConsole(final String line){
		final IDocument doc = getDocument();

		disableEditing();
		
		Display.getCurrent().syncExec(new Runnable(){
			public void run(){
				try{
					
					doc.replace(doc.getLength(), 0, line);
					doc.replace(doc.getLength(), 0, "\n");
					
				}catch(BadLocationException blex){
					// Ignore, never happens.
				}
			}
		});
		
		enableEditing();
	}
	
	private void emitPrompt(){
		disableEditing();
		
		writeToConsole(prompt);
		
		enableEditing();
	}
	
	private void emitContinuationPrompt(){
		disableEditing();
		
		writeToConsole(continuationPrompt);
		
		enableEditing();
	}
	
	private void enableEditing(){
		Display.getCurrent().syncExec(new Runnable(){
			public void run(){
				documentListener.enable();
				page.getViewer().setEditable(true);
			}
		});
	}
	
	private void disableEditing(){
		Display.getCurrent().syncExec(new Runnable(){
			public void run(){
				page.getViewer().setEditable(false);
				documentListener.disable();
			}
		});
	}
	
	public OutputStream getConsoleOutputStream(){
		return new ConsoleOutputStream(this);
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
	
	private static class InterpreterConsolePage extends TextConsolePage{

		public InterpreterConsolePage(TextConsole console, IConsoleView view){
			super(console, view);
		}
	}
	
	private static class InterpreterConsolePartitioner extends FastPartitioner implements IConsoleDocumentPartitioner{
		
		public InterpreterConsolePartitioner(){
			super(new PartitionScanner(), new String[]{});
		}

		public StyleRange[] getStyleRanges(int offset, int length){
			return new StyleRange[]{ new StyleRange(offset, length, null, null, SWT.NO) };
		}

		public boolean isReadOnly(int offset){
			return false;
		}
		
		private static class PartitionScanner extends RuleBasedPartitionScanner{
			
			public PartitionScanner(){
				super();
			}
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

		public void documentChanged(DocumentEvent event){ // TODO Run asynch.
			if(!enabled) return;
			
			String text = event.getText();
			
			buffer.append(text);
			
			if(text.charAt(text.length() - 1) == '\n'){ // TODO Make portable.
				String command = buffer.toString();
				
				console.commandHistory.addToHistory(command);
				
				try{
					boolean promptType = console.interpreter.execute(command);
					if(promptType){
						console.consoleOutputStream.print();
						console.writeToConsole(console.interpreter.getOutput());
						console.emitPrompt();
					}else{
						console.emitContinuationPrompt();
					}
				}catch(CommandExecutionException cex){
					console.writeToConsole(cex.getMessage());
				}
			}
		}
	}
}
