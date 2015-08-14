/*******************************************************************************
 * Copyright (c) 2009-2012 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Emilie Balland - (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
*******************************************************************************/
package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.Assert;
import org.eclipse.imp.runtime.RuntimePlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.IConsoleDocumentPartitioner;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.console.TextConsolePage;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.part.IPageBookViewPage;
import org.rascalmpl.interpreter.result.AbstractFunction;
import org.rascalmpl.interpreter.utils.Timing;
import org.rascalmpl.uri.LinkifiedString;

public class InteractiveInterpreterConsole extends TextConsole implements IInterpreterConsole{
	private final static String CONSOLE_TYPE = InteractiveInterpreterConsole.class.getName();
	
	private final static String COMMAND_TERMINATOR = System.getProperty("line.separator");

	// enable this to print the time each command took to execute
	public static final boolean PRINTCOMMANDTIME = false;
	
	private final IInterpreter interpreter;
	
	private final CommandExecutor commandExecutor;
	private final CommandHistory commandHistory;
	private final CommandFragmentCompletion completion;
	private final ConsoleDocumentListener documentListener;
	private final ConsoleOutputStream consoleOutputStream;
	
	private final InterpreterConsolePartitioner partitioner;
	private volatile TextConsolePage page;
	
	private final String prompt;
	private final String continuationPrompt;
	
	private int inputOffset;
	private String currentContent;
	protected final List<IHyperlink> currentHyperlinks = new ArrayList<IHyperlink>();
	protected final List<Integer> currentHyperlinkOffsets = new ArrayList<Integer>();
	protected final List<Integer> currentHyperlinkLengths = new ArrayList<Integer>();
	
	private volatile boolean promptInitialized;
	private volatile boolean terminated;

	/**
	 * Queue for commands that are invoked through {@link #executeCommand(String)}.
	 * 
	 * Purpose of the queue is:
	 * <ul>
	 * 	<li>to collect commands that were issued before the console was initialized</li>
	 * 	<li>avoiding race conditions due to consecutive document updates</li>
	 * </ul>
	 */
	private final Queue<String> delayedCommandQueue = new ConcurrentLinkedQueue<String>();

	private boolean completing = false;

	
	public InteractiveInterpreterConsole(IInterpreter interpreter, String name, String prompt, String continuationPrompt){
		super(name, CONSOLE_TYPE, null, false);
		
		this.interpreter = interpreter;
		commandExecutor = new CommandExecutor(this);
		consoleOutputStream = new ConsoleOutputStream(this);
		interpreter.setConsole(this); 
		
		this.prompt = prompt;
		this.continuationPrompt = continuationPrompt;
		
		commandHistory = new CommandHistory();
		completion = new CommandFragmentCompletion(interpreter.getEval());
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
		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				setFont(JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT));
			}
		});
	}
	
	public void clearConsole() {
		super.clearConsole();
		currentHyperlinks.clear();
		currentHyperlinkLengths.clear();
		currentHyperlinkOffsets.clear();
		currentContent = "";
	}
	
	public void initializeConsole(){
		final Thread commandExecutorThread = new Thread(commandExecutor);
		commandExecutorThread.setDaemon(true);
		commandExecutorThread.setName("Console Command Executor");
		
		new Thread(){
			public void run(){
				do{
					Thread.yield();
				}while(page == null);
				
				disableEditing();
				emitPrompt();	

				/**
				 * <code>promptInitialized</code> is set after
				 * <code>page</code> was initialized and the console is ready
				 * for input (i.e. the prompt was printed). This are the
				 * preconditions that commands from the
				 * <code>delayedCommandQueue</code> might be processed.
				 */
				promptInitialized = true;
				
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
	
	@Override
	public boolean isTerminated() {
		return terminated;
	}	
	
	@Override
	public void terminate(){
		boolean wasTerminated;
		
		synchronized (this) {
			wasTerminated = terminated;
			terminated = true;
		}
		
		if (!wasTerminated) {
			partitionerFinished();
			documentListener.deregisterListener();
			
			commandExecutor.terminate();
			interpreter.interrupt();
			interpreter.terminate();
		}
	}
	
	public void printTrace() {
		writeToConsole(interpreter.getTrace().toLinkedString(), true);
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

	public CommandFragmentCompletion getCompletion() {
		return completion;
	}
	
	protected IConsoleDocumentPartitioner getPartitioner(){
		return partitioner;
	}

	public IPageBookViewPage createPage(IConsoleView view){
		return (page = new InterpreterConsolePage(this, view));
	}

	public void addExistingHyperlink(IHyperlink hyperlink, int offset, int length) throws BadLocationException {
		super.addHyperlink(hyperlink, offset, length);
	}
	@Override
	public void addHyperlink(IHyperlink hyperlink, int offset, int length) throws BadLocationException {
		currentHyperlinks.add(hyperlink);
		currentHyperlinkOffsets.add(offset);
		currentHyperlinkLengths.add(length);
		super.addHyperlink(hyperlink, offset, length);
	}

	private void writeToConsole(final String line, final boolean terminateLine){
		final LinkifiedString linkedLine = new LinkifiedString(line);
		Display.getDefault().asyncExec(new Runnable(){
			public void run(){
				try{
					IDocument doc = getDocument();
					doc.replace(doc.getLength(), 0, linkedLine.getString());
					if (linkedLine.containsLinks()) {
						int fragmentOffsetStart = doc.getLength() - linkedLine.getString().length();
						for (int i = 0; i < linkedLine.linkCount(); i++) {
							String target = linkedLine.linkTarget(i);
							if (target.startsWith("|stdin") || target.startsWith("|unknown")) continue; // do not make a link out of the stdin/unknown console loc
							IHyperlink h;
							int offset = fragmentOffsetStart + linkedLine.linkOffset(i);
							int length = linkedLine.linkLength(i) - 1;
              
							if (target.startsWith("|")) {
								// source location
								h = new RascalHyperlink(InteractiveInterpreterConsole.this, offset, length, target, getInterpreter().getEval().getStdErr());
							}
							else {
								h = new WebHyperlink(target);
							}
							
              addHyperlink(h, offset, length);
						}
					}
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

				/*
				 * Here as well it's the most convenient and non-invasive place
				 * to perform this action, because the console only reacts to
				 * user-generated document update events when it's active {@see
				 * ConsoleDocumentListener#documentChanged(DocumentEvent)}.
				 */
				consumeNextQueuedCommand();
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
	protected int getCurrentCursorPosition() {
		TextConsoleViewer consoleViewer = page.getViewer();
		StyledText styledText = consoleViewer.getTextWidget();
		return styledText.getCaretOffset() - getInputOffset();
	}
	
	protected void printOutput(String output){
		writeToConsole(output, false);
	}
	
	protected void printOutput(){
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
	
	public void executeCommand(String cmd){

		/*
		 * Removing a) preceding white space and b) empty lines,
		 * and c) lines that only contain white space.
		 *  
		 * Using inline modifier "(?m)" to enable multiline mode.
		 */
		cmd = cmd.replaceAll("(?m)^\\s+", "");
		
		/*
		 * Adding command terminator to ensure execution.
		 */
		if(!cmd.endsWith(COMMAND_TERMINATOR)) {
			cmd = cmd.concat(COMMAND_TERMINATOR);
		}
	
		delayedCommandQueue.add(cmd);

		Display.getDefault().asyncExec(new Runnable(){
			public void run() {
				
				/*
				 * Execute queued command directly iff console is idle. Otherwise
				 * it will be executed upon next {@link #enableEditing()}.
				 */
				if (promptInitialized && page.getViewer().isEditable()) { consumeNextQueuedCommand(); }
			}
		});
	
	}

	/*
	 * Execute next document update (i.e. that was queued from
	 * {@link InteractiveInterpreterConsole#executeCommand(String)}
	 * if present.
	 */
	protected void consumeNextQueuedCommand() {
		Assert.isNotNull(page);
		Assert.isTrue(page.getViewer().isEditable());
		Assert.isTrue(promptInitialized);
		
		IDocument doc = getDocument();
		
		String command = delayedCommandQueue.poll();
		if (command != null) {
			boolean validCommand = true;
			try{
				if (command.startsWith("main();")) {
					// this command is special cased to make sure a main is defined
					// Paul has indicated new users are confused by the errors caused
					// by the lack of a main() method in the "executed" module.
					List<AbstractFunction> collection = new ArrayList<AbstractFunction>();
					getInterpreter().getEval().getCurrentEnvt().getAllFunctions("main", collection);
					if (collection.isEmpty()) {
						// main is not defined, dropping the command
						getInterpreter().getEval().getStdErr().println("Dropping command: \"main();\" since main is not defined");
						validCommand = false;
					}
				}
				if (validCommand) {
					doc.replace(inputOffset, doc.getLength() - inputOffset, command);
				}
			}catch(BadLocationException blex){
				// Ignore, can't happen.
			}
			if (validCommand) {
				moveCaretTo(doc.getLength());
			}
		}
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
				for (int i =0; i < currentHyperlinks.size(); i++) {
					try {
						addExistingHyperlink(currentHyperlinks.get(i), currentHyperlinkOffsets.get(i), currentHyperlinkLengths.get(i));
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}
				
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
				documentListener.reset();
				
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

	public void replaceCompletion(final int completionOffset, final int completionPreviousLength, final String newSuggestion) {
		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				setCompletionReplace();
				
				IDocument doc = getDocument();
				try{
					int subOffset = inputOffset + completionOffset;
					doc.replace(subOffset, completionPreviousLength, newSuggestion);
				}catch(BadLocationException blex){
					// Ignore, can't happen.
				}
				finally {
					unsetCompletionReplace();
				}
				
				moveCaretTo(inputOffset + completionOffset + newSuggestion.length());
			}
		});
	}
	
	protected void unsetCompletionReplace() {
		this.completing = false;
		
	}

	protected void setCompletionReplace() {
		this.completing = true;
	}

	// Only call from inside the UI-thread.
	private void moveCaretTo(int index){
		TextConsoleViewer consoleViewer = page.getViewer();
		StyledText styledText = consoleViewer.getTextWidget();
		
		styledText.setCaretOffset(index);
		consoleViewer.revealRange(index, 0);
	}
	
 public void setSelection(final int offset, final int length) {
   Display.getDefault().asyncExec(new Runnable(){
    @Override
    public void run() {
      TextConsoleViewer consoleViewer = page.getViewer();
      StyledText styledText = consoleViewer.getTextWidget();
      
      styledText.setSelectionRange(offset, length);
      consoleViewer.revealRange(offset, length);
    }
   });
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
		
		private final InteractiveInterpreterConsole console;
		
		private boolean enabled;

		private final PrintStream backup;
		
		private ConsoleOutputStream(InteractiveInterpreterConsole console){
			super();
			
			this.console = console;
			
			enabled = false;
			
			this.backup = RuntimePlugin.getInstance().getConsoleStream();
		}
		
		public synchronized void write(byte[] bytes, int offset, int length) throws IOException {
			if(!enabled){
				backup.write(bytes,offset,length);
				return;
			}
			if (length > 0) {
				try {
					console.writeToConsole(new String(bytes, offset, length, "UTF16"), false);
				} catch (UnsupportedEncodingException e) {
				}
			}
		}
		
		public synchronized void write(int arg) throws IOException{
			if(!enabled){
				backup.write(arg);
				return;
			}
			
			if(arg == '\n' || arg == '\r'){ // If we encounter a new-line, print the content of the buffer.
				return;
			}
			console.writeToConsole(new String(new byte[] {(byte)arg}), false);
			
		}
		
		public synchronized void enable(){
			enabled = true;
		}
		
		public synchronized void disable(){
			enabled = false;
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
		
		private final StringBuilder buffer;
		
		public ConsoleDocumentListener(InteractiveInterpreterConsole console){
			super();
			
			this.console = console;
			
			buffer = new StringBuilder();
			
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
		
		public synchronized void documentChanged(DocumentEvent event){
			if(!enabled) return;
			if(!console.completing) {
				console.completion.resetSearch();
			}
			
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
		
		public synchronized String getCurrentBufferContent(){
			return buffer.toString();
		}
		
		public void queue(String command, int commandStartOffset){
			if(!command.equals(COMMAND_TERMINATOR)) console.commandHistory.addToHistory(command);
			console.commandExecutor.queue(command, commandStartOffset);
		}
		
		private void execute(){
			console.commandExecutor.execute();
			console.commandHistory.resetSearch();
			console.completion.resetSearch();
		}
		
		public synchronized void reset(){
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
							Timing tm = new Timing();
							tm.start();
							completeCommand = console.interpreter.execute(command.command);
							long duration = tm.duration();
							
							if(completeCommand){
								console.printOutput(console.interpreter.getOutput());
								if (PRINTCOMMANDTIME) {
									console.printOutput("Time taken: " + duration + "ms.\n");
								}
								completeCommandStartOffset = -1; // Reset offset.
							}else{
								console.printOutput();
							}
						}catch(CommandExecutionException ceex){
							console.printOutput(ceex.getMessage());
							int errorOffset = ceex.getOffset();
							int errorLength = ceex.getLength();
							if (errorOffset != -1 && errorLength != -1) {
								console.setError(completeCommandStartOffset + errorOffset, errorLength);
							}
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
