package org.rascalmpl.eclipse.console.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.internal.console.IOConsolePage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.eclipse.console.ConsoleFactory.IRascalConsole;
import org.rascalmpl.eclipse.console.RascalScriptInterpreter;
import org.rascalmpl.interpreter.Evaluator;

/**
 * A simpler implementation of a console that does not support large output to be written, just
 * a repl with limited output capabilities. Larger output is supposed to be written elsewhere!
 */
public class RascalIOConsole extends IOConsole implements IInterpreterConsole, IRascalConsole {
	private final IInterpreter interpreter;
	private final String prompt;
	private final String continuationPrompt;
	private final Thread handlerThread;

	public RascalIOConsole(IInterpreter interpreter, Evaluator eval, String name, String prompt, String continuationPrompt) {
		super(name, name, Activator.getInstance().getImageRegistry().getDescriptor(IRascalResources.RASCAL_DEFAULT_IMAGE), "UTF8", true);
		this.interpreter = interpreter;
		this.interpreter.setConsole(this);
		this.interpreter.initialize(eval);
		this.prompt = prompt;
		this.continuationPrompt = continuationPrompt;
		
		handlerThread = new Thread(new InputHandler(getInputStream(), newOutputStream()));
		handlerThread.setName("IO input handler");
		handlerThread.start();
	}
	
	@Override
	public void terminate() {
		interpreter.terminate();
	}

	@Override
	public void executeCommand(String command) {
		
	}

	@Override
	public IInterpreter getInterpreter() {
		return interpreter;
	}

	@Override
	public boolean hasHistory() {
		return false;
	}

	@Override
	public CommandHistory getHistory() {
		return new CommandHistory();
	}
	
	public class InputHandler implements Runnable {
		private final PrintWriter output;
		private final BufferedReader input;

		public InputHandler(IOConsoleInputStream inputStream, IOConsoleOutputStream ioConsoleOutputStream) {
			this.input = new BufferedReader(new InputStreamReader(inputStream));
			this.output = new PrintWriter(ioConsoleOutputStream);
		}

		@Override
		public void run() {
			IInterpreter interpreter = RascalIOConsole.this.interpreter;
			output.print(RascalIOConsole.this.prompt);
			output.flush();
			
			while (true) {
				try {
					String line = input.readLine();
					
					if (interpreter.execute(line)) {
						String result = interpreter.getOutput();
						output.append(result);
						output.append('\n');
						output.print(RascalIOConsole.this.prompt);
						output.flush();
					} 
					else {
						output.print(RascalIOConsole.this.continuationPrompt);
						output.flush();
					}
				} catch (IOException e) {
					Activator.getInstance().logException("unexpected issue in console input reader", e);
				} catch (CommandExecutionException e) {
					e.printStackTrace(output);
					output.print('\n');
					output.print(RascalIOConsole.this.prompt);
					output.flush();
				} catch (TerminationException e) {
					RascalIOConsole.this.terminate();
					break;
				}
			}
		}
	}

	@Override
	public RascalScriptInterpreter getRascalInterpreter() {
		// TODO Auto-generated method stub
		return null;
	}
}
