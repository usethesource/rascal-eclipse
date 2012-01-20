package org.rascalmpl.eclipse.console.internal;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

public class StdAndErrorViewPart extends ViewPart implements Pausable {
	public static final String ID = "rascal-eclipse.outputview";
	public static final int BUFFER_SIZE = Math.min(8*1024*1024,highestPowerOf2Below(Text.LIMIT/4)); // in bytes,text == UTF16, and we double buffer the text control
	public static final int STD_OUT_BUFFER_SIZE = BUFFER_SIZE; 
	public static final int STD_ERR_BUFFER_SIZE = BUFFER_SIZE;	
	public static final long BUFFER_FLUSH_INTERVAL = 50; // in milliseconds
	private static boolean paused;
	private OutputWidget stdOut;
	private OutputWidget stdErr;

	// since SWT likes to dispose our ViewPart under Mac OSX, we have to store the output streams and pipes
	// because we cannot update the references in the Evaluators already running.
	private static final StreamConnection outStreamConnection;
	private static final StreamConnection errStreamConnection;

	
	private static class AlwaysPaused implements PausableOutput{
		@Override
		public boolean isPaused() {
			return true;
		}
		@Override
		public void output(byte[] b) throws IOException {
			throw new Error("Cannot write to always paused outputstream!");
		}
		
	}
	
	static {
		outStreamConnection = connectBuffers(new AlwaysPaused(), "stdOut", STD_OUT_BUFFER_SIZE);
		errStreamConnection = connectBuffers(new AlwaysPaused(), "stdErr", STD_ERR_BUFFER_SIZE);
	}
	
	private static class StreamConnection{
		private TimedBufferedPipe uiPipe;
		private OutputStream stream;
		
		public StreamConnection(TimedBufferedPipe uiPipe, OutputStream stream) {
			this.uiPipe = uiPipe;
			this.stream = stream;
		}
		
		public OutputStream getStream() {
			return stream;
		}
		
		public TimedBufferedPipe getUiPipe() {
			return uiPipe;
		}
	}
	
	private static StreamConnection connectBuffers(PausableOutput pausableOutput, String alias, int bufSize){
		// Why this complicated setup?
		// 
		// con: rascal console
		// pip: buffer pipe (separate thread)
		// buf: buffer
		// ui: ui-thread
		//
		// ascii art:
		//
		//  con1---+               
		//  con2-+ |
		//       +-+---> buf1 <-- pip1 --> buf2 <-- pip2 --> ui
		//  con3-+ |
		//  con4---+
		//
		// our console buffer (buf1) is written to from different threads and evaluators
		// pip1 reads from buf1 (which blocks the writers while reading)
		// pip1 than writes to buf2 
		// pip2 than reads from buf2 (blocking again) and transforms it into a string
		// pip2 than schedules the writing of this string on the UI thread.
		//
		// We have this relatively complicated setup to make sure the UI thread doesn't block while
		// the console is writing in the buffer, or the other way around.
		
		TimedBufferedPipe uiPipe = new TimedBufferedPipe(BUFFER_FLUSH_INTERVAL,pausableOutput, "UI Pipe " + alias);
		ConcurrentCircularOutputStream uiBuffer = new ConcurrentCircularOutputStream(bufSize, uiPipe);
		uiPipe.initializeWithStream(uiBuffer);
		
		TimedBufferedPipe consolePipe = new TimedBufferedPipe(BUFFER_FLUSH_INTERVAL,new PausableOutputBuffer(uiBuffer, uiPipe), "Console Pipe " + alias);
		ConcurrentCircularOutputStream consoleBuffer = new ConcurrentCircularOutputStream(bufSize, consolePipe);
		consolePipe.initializeWithStream(consoleBuffer);
		return new StreamConnection(uiPipe, consoleBuffer);
	}
	
	public StdAndErrorViewPart() { 
		super(); 
	}
	
	private static int highestPowerOf2Below(int i) {
		int j = 1;
		int prev = 0;
		while(j < i){
			prev = j;
			j*=2;
		}
		return prev;
	}

	class PauseOutputAction extends Action{
		
		public PauseOutputAction() {
			super("",IAction.AS_RADIO_BUTTON);
			setText("Pause output");
			
		}
		
		@Override
		public void run() {
			paused = !paused;
			setChecked(paused);
		}
		
	}
	
	class ClearAction extends Action{
		
		public ClearAction() {
			setText("Clear output");
			setToolTipText("Clears the output");
		}
		
		@Override
		public void run() {
			stdOut.clear();
			stdErr.clear();
		}
		
	}
	
	private OutputWidget makeWidget(StreamConnection connection,Composite parent,Color c, int bufSize, boolean showAlways){
		OutputWidget widget = new OutputWidget(parent, c, bufSize, showAlways,this);
		connection.getUiPipe().setTarget(widget);
		return widget;
	}
	
	

	@Override
	public void createPartControl(Composite parent) {
		SashCompose totalWidget = new SashCompose(parent);
		IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();
		toolbar.add(new PauseOutputAction());
		toolbar.add(new ClearAction());
		stdOut = makeWidget(outStreamConnection,totalWidget.getSashForm(), Display.getCurrent().getSystemColor(SWT.COLOR_BLACK),
				STD_OUT_BUFFER_SIZE, true);
		stdErr = makeWidget(errStreamConnection,totalWidget.getSashForm(), Display.getCurrent().getSystemColor(SWT.COLOR_RED),
				STD_ERR_BUFFER_SIZE, true);
		
	}
	

	@Override
	public void setFocus() {
	}

	@Override
	public boolean isPaused() {
		return paused;
	}
	
	private class SashCompose extends Composite{
		private SashForm sashForm;

		public SashCompose(Composite parent) {
			super(parent,SWT.NONE);
			setLayout(new FillLayout());
			this.sashForm = new SashForm(this, SWT.VERTICAL | SWT.H_SCROLL | SWT.V_SCROLL);
		}
		
		public SashForm getSashForm() {
			return sashForm;
		}
	}
	
	public static OutputStream getStdOut(){
		return outStreamConnection.getStream();
	}
	
	public static OutputStream getStdErr(){
		return errStreamConnection.getStream();
	}

	
}
