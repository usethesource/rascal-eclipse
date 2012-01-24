package org.rascalmpl.eclipse.console.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IValueFactory;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.internal.console.IOConsolePage;
import org.eclipse.ui.part.IPageBookViewPage;
import org.rascalmpl.eclipse.Activator;
import org.rascalmpl.eclipse.IRascalResources;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.control_exceptions.Throw;
import org.rascalmpl.interpreter.result.ICallableValue;
import org.rascalmpl.interpreter.result.Result;
import org.rascalmpl.interpreter.staticErrors.StaticError;

/**
 * A simpler implementation of a console that does not support large output to be written, just
 * a repl with limited output capabilities. Larger output is supposed to be written elsewhere!
 */
public class RascalIOConsole extends IOConsole {
	private final Thread handlerThread;
	private TextConsoleViewer viewer;

	public RascalIOConsole(String name, String startText, TypeFactory tf, IValueFactory vf, ICallableValue newLineCallback) {
		super(name, name, Activator.getInstance().getImageRegistry().getDescriptor(IRascalResources.RASCAL_DEFAULT_IMAGE), "UTF8", true);
		handlerThread = new Thread(new InputHandler(getInputStream(), newOutputStream(), newLineCallback, startText, tf, vf));
		handlerThread.setName("IO input handler");
		handlerThread.start();
	}
	@Override
    public IPageBookViewPage createPage(IConsoleView view) {
        @SuppressWarnings("restriction")
		final IOConsolePage page = new IOConsolePage(this, view) {
        	private org.eclipse.ui.console.TextConsoleViewer cached;
        	@Override
        	protected org.eclipse.ui.console.TextConsoleViewer createViewer(org.eclipse.swt.widgets.Composite parent) {
        		if (cached == null) {
        			cached = super.createViewer(parent);
        		}
        		return cached;
        	};
        };
        this.viewer = page.getViewer();
        getDocument().addDocumentListener(new IDocumentListener() {
			@Override
			public void documentChanged(DocumentEvent event) {
				if (viewer == null) {
					viewer = page.getViewer();
				}
				if (viewer != null) {
					//viewer.setSelectedRange(getDocument().getLength() - 1, -1);
				}
			}

			@Override
			public void documentAboutToBeChanged(DocumentEvent event) { }
		});
        
        return page;
    }
	
	@Override
	protected void dispose() {
		// TODO Auto-generated method stub
		this.handlerThread.interrupt();
		super.dispose();
	}

	public class InputHandler implements Runnable {
		private final PrintWriter output;
		private final BufferedReader input;
		private final ICallableValue callback;
		private final String startText;
		private final TypeFactory tf;
		private final IValueFactory vf;

		public InputHandler(IOConsoleInputStream inputStream, IOConsoleOutputStream ioConsoleOutputStream, ICallableValue newLineCallback, String startText, TypeFactory tf, IValueFactory vf) {
			this.input = new BufferedReader(new InputStreamReader(inputStream));
			this.output = new PrintWriter(ioConsoleOutputStream);
			this.callback = newLineCallback;
			this.startText = startText;
			this.tf = tf;
			this.vf = vf;
		}

		@Override
		public void run() {
			final Evaluator eval = callback.getEval();
			output.print(startText);
			output.flush();
			
			while (!output.checkError()) {
				int append = 0;
				try {
					String line = input.readLine();
					append += line.length();
					Result<IValue> result = null;
					try {
						synchronized (eval) {
							result = callback.call(new Type[] { tf.stringType() }, new IValue[] { vf.string(line) });
						}
					} catch (Throw e) {
						e.printStackTrace(eval.getStdErr());
						eval.getStdErr().printf("Callback error: " + e.getMessage() + " " + e.getTrace());
					} catch (StaticError e) {
						eval.getStdErr().printf("Static callback error: " + e.getMessage());
						e.printStackTrace(eval.getStdErr());
					}
					if (result != null && result.getValue().getType().isStringType()) {
						String actualResult = ((IString)result.getValue()).getValue();
						append = actualResult.length();
						output.print(actualResult);
						output.flush();
					}
					else {
						Activator.getInstance().logException("The console callback did not return a string?", new RuntimeException());
					}
				} catch (IOException e) {
					if (!e.getMessage().contains("Stream Closed")) {
						Activator.getInstance().logException("unexpected issue in console input reader", e);
					}
				}
				if (viewer != null) {
					final int finalAppend = append;
					Display.getDefault().asyncExec( new Runnable() {
						@Override
						public void run() {
							viewer.setSelectedRange(getDocument().getLength() + finalAppend - 1, -1);
						}
					});
				}
			}
		}
	}
}
